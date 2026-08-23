package ai.account;

import ai.common.db.CuihuaDataSource;
import ai.common.db.HikariDS;
import ai.utils.AiGlobal;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Database boundary between the Cuihua-owned user view and LinkMind-owned
 * sessions and client tokens. LinkMind never writes to the Cuihua database.
 */
@Slf4j
public class CuihuaAccountDao {
    private static final Object SCHEMA_LOCK = new Object();
    private static volatile boolean schemaReady;
    private static final List<String> USER_VIEW_COLUMNS = Arrays.asList(
            "user_id", "username", "email", "display_name", "password_hash",
            "role", "status", "locked_until", "created_at");
    private static final Pattern ARGON2ID_HASH = Pattern.compile(
            "^\\$argon2id\\$v=\\d+\\$m=\\d+,t=\\d+,p=\\d+\\$[A-Za-z0-9+/]+\\$[A-Za-z0-9+/]+$");

    /** Resets the local LinkMind schema cache after a test replaces its SQLite pool. */
    public static void resetSchemaForTests() {
        synchronized (SCHEMA_LOCK) {
            schemaReady = false;
        }
    }

    private static final String CREATE_CLIENT_TOKEN_TABLE = ""
            + "CREATE TABLE IF NOT EXISTS linkmind_client_api_key ("
            + " id INTEGER PRIMARY KEY AUTOINCREMENT,"
            + " user_id TEXT NOT NULL,"
            + " token_hash TEXT NOT NULL UNIQUE,"
            + " token_prefix TEXT NOT NULL,"
            + " name TEXT NOT NULL DEFAULT '',"
            + " status INTEGER NOT NULL DEFAULT 1,"
            + " created_at INTEGER NOT NULL,"
            + " expires_at INTEGER,"
            + " last_used_at INTEGER"
            + ")";

    private static final String CREATE_LOGIN_SESSION_TABLE = ""
            + "CREATE TABLE IF NOT EXISTS linkmind_login_session ("
            + " id INTEGER PRIMARY KEY AUTOINCREMENT,"
            + " user_id TEXT NOT NULL,"
            + " session_hash TEXT NOT NULL UNIQUE,"
            + " expires_at INTEGER NOT NULL,"
            + " created_at INTEGER NOT NULL,"
            + " last_seen_at INTEGER"
            + ")";

    /** Creates LinkMind-owned tables locally and validates the Cuihua-owned view. */
    public void initialize() throws SQLException {
        ensureLinkMindSchema();
        validateCuihuaView();
    }

    private void ensureLinkMindSchema() throws SQLException {
        if (schemaReady) {
            return;
        }
        synchronized (SCHEMA_LOCK) {
            if (schemaReady) {
                return;
            }
            try (Connection conn = HikariDS.getConnection(AiGlobal.DEFAULT_DB);
                 Statement statement = conn.createStatement()) {
                statement.executeUpdate(CREATE_CLIENT_TOKEN_TABLE);
                statement.executeUpdate(CREATE_LOGIN_SESSION_TABLE);
                statement.executeUpdate("CREATE INDEX IF NOT EXISTS idx_linkmind_client_api_key_user_id "
                        + "ON linkmind_client_api_key(user_id)");
                statement.executeUpdate("CREATE INDEX IF NOT EXISTS idx_linkmind_login_session_user_id "
                        + "ON linkmind_login_session(user_id)");
                schemaReady = true;
            }
        }
    }

    public CuihuaUser findActiveUserByLogin(String login) throws SQLException {
        if (isBlank(login)) {
            return null;
        }
        String sql = "SELECT user_id, username, email, display_name, password_hash, role, status, locked_until, created_at "
                + "FROM " + CuihuaDataSource.getUserView() + " WHERE (username = ? OR email = ?) "
                + "AND LOWER(status) = 'active' "
                + "AND (locked_until IS NULL OR datetime(locked_until) <= datetime('now')) LIMIT 1";
        try (Connection conn = CuihuaDataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, login.trim());
            ps.setString(2, login.trim());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapUser(rs) : null;
            }
        }
    }

    public CuihuaUser findActiveUserById(String userId) throws SQLException {
        if (isBlank(userId)) {
            return null;
        }
        String sql = "SELECT user_id, username, email, display_name, password_hash, role, status, locked_until, created_at "
                + "FROM " + CuihuaDataSource.getUserView() + " WHERE user_id = ? AND LOWER(status) = 'active' "
                + "AND (locked_until IS NULL OR datetime(locked_until) <= datetime('now')) LIMIT 1";
        try (Connection conn = CuihuaDataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId.trim());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapUser(rs) : null;
            }
        }
    }

    public LinkMindClientToken findClientToken(String tokenHash) throws SQLException {
        if (isBlank(tokenHash)) {
            return null;
        }
        ensureLinkMindSchema();
        String sql = "SELECT id, user_id, token_prefix, name, status, created_at, expires_at, last_used_at "
                + "FROM linkmind_client_api_key WHERE token_hash = ? LIMIT 1";
        try (Connection conn = HikariDS.getConnection(AiGlobal.DEFAULT_DB);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tokenHash);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapClientToken(rs) : null;
            }
        }
    }

    public void insertClientToken(LinkMindClientToken token, String tokenHash) throws SQLException {
        ensureLinkMindSchema();
        String sql = "INSERT INTO linkmind_client_api_key(user_id, token_hash, token_prefix, name, status, created_at, expires_at) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = HikariDS.getConnection(AiGlobal.DEFAULT_DB);
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, token.getUserId());
            ps.setString(2, tokenHash);
            ps.setString(3, token.getTokenPrefix());
            ps.setString(4, token.getName());
            ps.setInt(5, token.isEnabled() ? 1 : 0);
            ps.setLong(6, token.getCreatedAt());
            if (token.getExpiresAt() == null) {
                ps.setObject(7, null);
            } else {
                ps.setLong(7, token.getExpiresAt());
            }
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    token.setId(keys.getLong(1));
                }
            }
        }
    }

    public List<LinkMindClientToken> listClientTokens(String userId) throws SQLException {
        if (isBlank(userId)) {
            return new ArrayList<LinkMindClientToken>();
        }
        ensureLinkMindSchema();
        String sql = "SELECT id, user_id, token_prefix, name, status, created_at, expires_at, last_used_at "
                + "FROM linkmind_client_api_key WHERE user_id = ? ORDER BY id DESC";
        List<LinkMindClientToken> tokens = new ArrayList<LinkMindClientToken>();
        try (Connection conn = HikariDS.getConnection(AiGlobal.DEFAULT_DB);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId.trim());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    tokens.add(mapClientToken(rs));
                }
            }
        }
        return tokens;
    }

    public LinkMindClientToken findClientToken(long id, String userId) throws SQLException {
        if (id <= 0 || isBlank(userId)) {
            return null;
        }
        ensureLinkMindSchema();
        String sql = "SELECT id, user_id, token_prefix, name, status, created_at, expires_at, last_used_at "
                + "FROM linkmind_client_api_key WHERE id = ? AND user_id = ? LIMIT 1";
        try (Connection conn = HikariDS.getConnection(AiGlobal.DEFAULT_DB);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.setString(2, userId.trim());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapClientToken(rs) : null;
            }
        }
    }

    public boolean updateClientToken(LinkMindClientToken token) throws SQLException {
        if (token == null || token.getId() <= 0 || isBlank(token.getUserId())) {
            return false;
        }
        ensureLinkMindSchema();
        String sql = "UPDATE linkmind_client_api_key SET name = ?, status = ?, expires_at = ? "
                + "WHERE id = ? AND user_id = ?";
        try (Connection conn = HikariDS.getConnection(AiGlobal.DEFAULT_DB);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, token.getName() == null ? "" : token.getName().trim());
            ps.setInt(2, token.isEnabled() ? 1 : 0);
            if (token.getExpiresAt() == null) {
                ps.setObject(3, null);
            } else {
                ps.setLong(3, token.getExpiresAt());
            }
            ps.setLong(4, token.getId());
            ps.setString(5, token.getUserId().trim());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean revokeClientToken(long id, String userId) throws SQLException {
        if (id <= 0 || isBlank(userId)) {
            return false;
        }
        ensureLinkMindSchema();
        try (Connection conn = HikariDS.getConnection(AiGlobal.DEFAULT_DB);
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE linkmind_client_api_key SET status = 0 WHERE id = ? AND user_id = ?")) {
            ps.setLong(1, id);
            ps.setString(2, userId.trim());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean deleteClientToken(long id, String userId) throws SQLException {
        if (id <= 0 || isBlank(userId)) {
            return false;
        }
        ensureLinkMindSchema();
        try (Connection conn = HikariDS.getConnection(AiGlobal.DEFAULT_DB);
             PreparedStatement ps = conn.prepareStatement(
                     "DELETE FROM linkmind_client_api_key WHERE id = ? AND user_id = ?")) {
            ps.setLong(1, id);
            ps.setString(2, userId.trim());
            return ps.executeUpdate() > 0;
        }
    }

    public void touchClientToken(long id) {
        if (id <= 0) {
            return;
        }
        try {
            ensureLinkMindSchema();
        } catch (SQLException e) {
            log.warn("prepare LinkMind client token schema failed: {}", e.getMessage());
            return;
        }
        try (Connection conn = HikariDS.getConnection(AiGlobal.DEFAULT_DB);
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE linkmind_client_api_key SET last_used_at = ? WHERE id = ?")) {
            ps.setLong(1, System.currentTimeMillis());
            ps.setLong(2, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            log.warn("update LinkMind client token last-used time failed: {}", e.getMessage());
        }
    }

    public void insertLoginSession(String userId, String sessionHash, long expiresAt) throws SQLException {
        ensureLinkMindSchema();
        String sql = "INSERT INTO linkmind_login_session(user_id, session_hash, expires_at, created_at, last_seen_at) "
                + "VALUES (?, ?, ?, ?, ?)";
        long now = System.currentTimeMillis();
        try (Connection conn = HikariDS.getConnection(AiGlobal.DEFAULT_DB);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId);
            ps.setString(2, sessionHash);
            ps.setLong(3, expiresAt);
            ps.setLong(4, now);
            ps.setLong(5, now);
            ps.executeUpdate();
        }
    }

    public CuihuaUser findActiveUserBySessionHash(String sessionHash) throws SQLException {
        if (isBlank(sessionHash)) {
            return null;
        }
        ensureLinkMindSchema();
        String userId = null;
        String sql = "SELECT user_id FROM linkmind_login_session "
                + "WHERE session_hash = ? AND expires_at > ? LIMIT 1";
        try (Connection conn = HikariDS.getConnection(AiGlobal.DEFAULT_DB);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, sessionHash);
            ps.setLong(2, System.currentTimeMillis());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    userId = rs.getString("user_id");
                }
            }
        }
        return userId == null ? null : findActiveUserById(userId);
    }

    public void touchLoginSession(String sessionHash) {
        if (isBlank(sessionHash)) {
            return;
        }
        try {
            ensureLinkMindSchema();
        } catch (SQLException e) {
            log.warn("prepare LinkMind login session schema failed: {}", e.getMessage());
            return;
        }
        try (Connection conn = HikariDS.getConnection(AiGlobal.DEFAULT_DB);
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE linkmind_login_session SET last_seen_at = ? WHERE session_hash = ?")) {
            ps.setLong(1, System.currentTimeMillis());
            ps.setString(2, sessionHash);
            ps.executeUpdate();
        } catch (SQLException e) {
            log.warn("update LinkMind session last-seen time failed: {}", e.getMessage());
        }
    }

    public void deleteLoginSession(String sessionHash) throws SQLException {
        if (isBlank(sessionHash)) {
            return;
        }
        ensureLinkMindSchema();
        try (Connection conn = HikariDS.getConnection(AiGlobal.DEFAULT_DB);
             PreparedStatement ps = conn.prepareStatement(
                     "DELETE FROM linkmind_login_session WHERE session_hash = ?")) {
            ps.setString(1, sessionHash);
            ps.executeUpdate();
        }
    }

    public static String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder result = new StringBuilder(bytes.length * 2);
            for (byte b : bytes) {
                result.append(String.format("%02x", b & 0xff));
            }
            return result.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is unavailable", e);
        }
    }

    private void validateCuihuaView() throws SQLException {
        String view = CuihuaDataSource.getUserView();
        try (Connection conn = CuihuaDataSource.getConnection()) {
            validateWalMode(conn);
            validateViewObject(conn, view);
            validateViewColumns(conn, view);
            validateViewRows(conn, view);
        }
    }

    private void validateWalMode(Connection conn) throws SQLException {
        try (Statement statement = conn.createStatement();
             ResultSet rs = statement.executeQuery("PRAGMA journal_mode")) {
            if (!rs.next() || !"wal".equalsIgnoreCase(rs.getString(1))) {
                throw new SQLException("Cuihua SQLite database must use WAL journal mode");
            }
        }
    }

    private void validateViewObject(Connection conn, String view) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT type FROM sqlite_master WHERE name = ? COLLATE BINARY LIMIT 1")) {
            ps.setString(1, view);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next() || !"view".equalsIgnoreCase(rs.getString("type"))) {
                    throw new SQLException("Cuihua user_view is missing or is not a view: " + view);
                }
            }
        }
    }

    private void validateViewColumns(Connection conn, String view) throws SQLException {
        List<String> actual = new ArrayList<String>();
        try (Statement statement = conn.createStatement();
             ResultSet rs = statement.executeQuery("PRAGMA table_info(" + view + ")")) {
            while (rs.next()) {
                actual.add(rs.getString("name"));
            }
        }
        if (!USER_VIEW_COLUMNS.equals(actual)) {
            throw new SQLException("Cuihua user_view columns must be exactly " + USER_VIEW_COLUMNS + ", actual " + actual);
        }
    }

    private void validateViewRows(Connection conn, String view) throws SQLException {
        String sql = "SELECT user_id, username, email, display_name, password_hash, role, status, locked_until, "
                + "created_at, typeof(created_at) AS created_at_type, datetime(locked_until) AS parsed_locked_until "
                + "FROM " + view;
        Set<String> userIds = new HashSet<String>();
        try (Statement statement = conn.createStatement();
             ResultSet rs = statement.executeQuery(sql)) {
            while (rs.next()) {
                String userId = requiredColumn(rs, "user_id", null);
                requiredColumn(rs, "username", userId);
                requiredColumn(rs, "email", userId);
                requiredColumn(rs, "display_name", userId);
                String passwordHash = requiredColumn(rs, "password_hash", userId);
                if (!ARGON2ID_HASH.matcher(passwordHash).matches()) {
                    throw invalidUser(userId, "password_hash must be a valid Argon2id encoded hash");
                }
                String role = requiredColumn(rs, "role", userId);
                if (!"system_admin".equals(role) && !"user".equals(role)) {
                    throw invalidUser(userId, "role must be system_admin or user");
                }
                if (!"active".equals(requiredColumn(rs, "status", userId))) {
                    throw invalidUser(userId, "status must be active");
                }
                String lockedUntil = rs.getString("locked_until");
                if (lockedUntil != null && rs.getString("parsed_locked_until") == null) {
                    throw invalidUser(userId, "locked_until is not a SQLite datetime");
                }
                if (!"integer".equals(rs.getString("created_at_type")) || rs.getLong("created_at") <= 0L) {
                    throw invalidUser(userId, "created_at must be a positive Unix millisecond integer");
                }
                if (!userIds.add(userId)) {
                    throw invalidUser(userId, "user_id is duplicated");
                }
            }
        }
    }

    private String requiredColumn(ResultSet rs, String column, String userId) throws SQLException {
        String value = rs.getString(column);
        if (isBlank(value)) {
            throw invalidUser(userId, column + " is required");
        }
        return value;
    }

    private SQLException invalidUser(String userId, String message) {
        return new SQLException("invalid Cuihua user_view row"
                + (userId == null ? "" : " for user_id " + userId) + ": " + message);
    }

    private CuihuaUser mapUser(ResultSet rs) throws SQLException {
        return CuihuaUser.builder()
                .userId(rs.getString("user_id"))
                .username(rs.getString("username"))
                .email(rs.getString("email"))
                .displayName(rs.getString("display_name"))
                .passwordHash(rs.getString("password_hash"))
                .role(rs.getString("role"))
                .status(rs.getString("status"))
                .lockedUntil(rs.getString("locked_until"))
                .createdAt(rs.getLong("created_at"))
                .build();
    }

    private LinkMindClientToken mapClientToken(ResultSet rs) throws SQLException {
        long expiresAt = rs.getLong("expires_at");
        boolean hasExpiresAt = !rs.wasNull();
        long lastUsedAt = rs.getLong("last_used_at");
        boolean hasLastUsedAt = !rs.wasNull();
        return LinkMindClientToken.builder()
                .id(rs.getLong("id"))
                .userId(rs.getString("user_id"))
                .tokenPrefix(rs.getString("token_prefix"))
                .name(rs.getString("name"))
                .enabled(rs.getInt("status") == 1)
                .createdAt(rs.getLong("created_at"))
                .expiresAt(hasExpiresAt ? expiresAt : null)
                .lastUsedAt(hasLastUsedAt ? lastUsedAt : null)
                .build();
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}

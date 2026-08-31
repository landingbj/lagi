package ai.account;

import ai.common.db.HikariDS;
import ai.common.db.TokenLinkDataSource;
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
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Storage boundary for the shared tokenlink database. Cuihua owns user rows;
 * Landing owns the {@code sk-...} user-key association and LinkMind owns only
 * its local browser sessions.
 */
@Slf4j
public class CuihuaAccountDao {
    private static final Object SCHEMA_LOCK = new Object();
    private static volatile boolean tokenLinkSchemaReady;
    private static volatile boolean sessionSchemaReady;

    private static final String CREATE_USERS_TABLE = ""
            + "CREATE TABLE IF NOT EXISTS users ("
            + " id TEXT NOT NULL PRIMARY KEY,"
            + " role TEXT NOT NULL,"
            + " username TEXT NOT NULL,"
            + " display_name TEXT,"
            + " password_hash TEXT NOT NULL,"
            + " user_login_status TEXT NOT NULL"
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

    /** Resets schema state after a test changes either SQLite data source. */
    public static void resetSchemaForTests() {
        synchronized (SCHEMA_LOCK) {
            tokenLinkSchemaReady = false;
            sessionSchemaReady = false;
        }
    }

    public void initialize() throws SQLException {
        ensureTokenLinkSchema();
        ensureSessionSchema();
    }

    public CuihuaUser findActiveUserByLogin(String login) throws SQLException {
        if (isBlank(login)) {
            return null;
        }
        String sql = "SELECT id, role, username, password_hash, user_login_status FROM users "
                + "WHERE username = ? LIMIT 1";
        try (Connection conn = TokenLinkDataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, login.trim());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapAvailableUser(rs) : null;
            }
        }
    }

    public CuihuaUser findActiveUserById(String userId) throws SQLException {
        if (isBlank(userId)) {
            return null;
        }
        String sql = "SELECT id, role, username, password_hash, user_login_status FROM users WHERE id = ? LIMIT 1";
        try (Connection conn = TokenLinkDataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId.trim());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapAvailableUser(rs) : null;
            }
        }
    }

    /** Resolves current display names for one page of shared TokenLink users. */
    public Map<String, String> findUserDisplayNames(Collection<String> userIds) throws SQLException {
        Map<String, CuihuaUserIdentity> identities = findUserIdentities(userIds);
        Map<String, String> displayNames = new LinkedHashMap<String, String>();
        for (CuihuaUserIdentity identity : identities.values()) {
            String displayName = identity.getDisplayName();
            if (isBlank(displayName)) {
                displayName = identity.getUsername();
            }
            if (!isBlank(displayName)) {
                displayNames.put(identity.getUserId(), displayName.trim());
            }
        }
        return displayNames;
    }

    /** Resolves current display name and login name for usage aggregation. */
    public Map<String, CuihuaUserIdentity> findUserIdentities(Collection<String> userIds) throws SQLException {
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyMap();
        }
        Set<String> normalizedIds = new LinkedHashSet<String>();
        for (String userId : userIds) {
            if (!isBlank(userId)) {
                normalizedIds.add(userId.trim());
            }
        }
        if (normalizedIds.isEmpty()) {
            return Collections.emptyMap();
        }

        StringBuilder sql = new StringBuilder("SELECT id, username, display_name FROM users WHERE id IN (");
        for (int i = 0; i < normalizedIds.size(); i++) {
            if (i > 0) {
                sql.append(',');
            }
            sql.append('?');
        }
        sql.append(')');

        Map<String, CuihuaUserIdentity> identities = new LinkedHashMap<String, CuihuaUserIdentity>();
        try (Connection conn = TokenLinkDataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int index = 1;
            for (String userId : normalizedIds) {
                ps.setString(index++, userId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String userId = rs.getString("id");
                    identities.put(userId, CuihuaUserIdentity.builder()
                            .userId(userId)
                            .username(optionalText(rs.getString("username")))
                            .displayName(optionalText(rs.getString("display_name")))
                            .build());
                }
            }
        }
        return identities;
    }

    /** The Alpha SSO rule: only a currently logged-in Cuihua system administrator may enter LinkMind. */
    public CuihuaUser findLoggedInSystemAdminById(String userId) throws SQLException {
        if (isBlank(userId)) {
            return null;
        }
        String sql = "SELECT id, role, username, password_hash, user_login_status FROM users "
                + "WHERE id = ? AND LOWER(role) = 'system_admin' "
                + "AND LOWER(user_login_status) = 'logged_in' LIMIT 1";
        try (Connection conn = TokenLinkDataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId.trim());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapAvailableUser(rs) : null;
            }
        }
    }

    public void insertLoginSession(String userId, String sessionHash, long expiresAt) throws SQLException {
        ensureSessionSchema();
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
        ensureSessionSchema();
        String sql = "SELECT user_id FROM linkmind_login_session WHERE session_hash = ? AND expires_at > ? LIMIT 1";
        try (Connection conn = HikariDS.getConnection(AiGlobal.DEFAULT_DB);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, sessionHash);
            ps.setLong(2, System.currentTimeMillis());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? findActiveUserById(rs.getString("user_id")) : null;
            }
        }
    }

    public void touchLoginSession(String sessionHash) {
        if (isBlank(sessionHash)) {
            return;
        }
        try {
            ensureSessionSchema();
            try (Connection conn = HikariDS.getConnection(AiGlobal.DEFAULT_DB);
                 PreparedStatement ps = conn.prepareStatement(
                         "UPDATE linkmind_login_session SET last_seen_at = ? WHERE session_hash = ?")) {
                ps.setLong(1, System.currentTimeMillis());
                ps.setString(2, sessionHash);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            log.warn("update LinkMind session last-seen time failed: {}", e.getMessage());
        }
    }

    public void deleteLoginSession(String sessionHash) throws SQLException {
        if (isBlank(sessionHash)) {
            return;
        }
        ensureSessionSchema();
        try (Connection conn = HikariDS.getConnection(AiGlobal.DEFAULT_DB);
             PreparedStatement ps = conn.prepareStatement("DELETE FROM linkmind_login_session WHERE session_hash = ?")) {
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

    private void ensureTokenLinkSchema() throws SQLException {
        if (tokenLinkSchemaReady) {
            return;
        }
        synchronized (SCHEMA_LOCK) {
            if (tokenLinkSchemaReady) {
                return;
            }
            try (Connection conn = TokenLinkDataSource.getConnection();
                 Statement statement = conn.createStatement()) {
                // The CREATE is schema bootstrap only; Cuihua exclusively owns user rows.
                statement.executeUpdate(CREATE_USERS_TABLE);
                validateUsersColumns(conn);
                tokenLinkSchemaReady = true;
            }
        }
    }

    private void ensureSessionSchema() throws SQLException {
        if (sessionSchemaReady) {
            return;
        }
        synchronized (SCHEMA_LOCK) {
            if (sessionSchemaReady) {
                return;
            }
            try (Connection conn = HikariDS.getConnection(AiGlobal.DEFAULT_DB);
                 Statement statement = conn.createStatement()) {
                statement.executeUpdate(CREATE_LOGIN_SESSION_TABLE);
                statement.executeUpdate("CREATE INDEX IF NOT EXISTS idx_linkmind_login_session_user_id "
                        + "ON linkmind_login_session(user_id)");
                sessionSchemaReady = true;
            }
        }
    }

    private void validateUsersColumns(Connection conn) throws SQLException {
        Set<String> columns = new HashSet<String>();
        try (Statement statement = conn.createStatement();
             ResultSet rs = statement.executeQuery("PRAGMA table_info(users)")) {
            while (rs.next()) {
                columns.add(rs.getString("name"));
            }
        }
        String[] required = {"id", "role", "username", "password_hash", "user_login_status"};
        for (String column : required) {
            if (!columns.contains(column)) {
                throw new SQLException("tokenlink users table is missing required column: " + column);
            }
        }
    }

    private CuihuaUser mapAvailableUser(ResultSet rs) throws SQLException {
        CuihuaUser user = CuihuaUser.builder()
                .userId(rs.getString("id"))
                .role(rs.getString("role"))
                .username(rs.getString("username"))
                .passwordHash(rs.getString("password_hash"))
                .userLoginStatus(rs.getString("user_login_status"))
                .build();
        if (isBlank(user.getUserId()) || isBlank(user.getUsername()) || isBlank(user.getRole())
                || isBlank(user.getPasswordHash()) || isBlank(user.getUserLoginStatus())) {
            return null;
        }
        if (!"user".equalsIgnoreCase(user.getRole())
                && !"admin".equalsIgnoreCase(user.getRole())
                && !"system_admin".equalsIgnoreCase(user.getRole())) {
            return null;
        }
        return user;
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static String optionalText(String value) {
        return isBlank(value) ? null : value.trim();
    }
}

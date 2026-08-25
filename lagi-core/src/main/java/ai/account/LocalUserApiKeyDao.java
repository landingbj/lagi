package ai.account;

import ai.common.db.TokenLinkDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/** Storage access for LinkMind-owned local user API keys in tokenlink.db. */
public class LocalUserApiKeyDao {
    private static final Object SCHEMA_LOCK = new Object();
    private static volatile boolean schemaReady;

    private static final String CREATE_TABLE = ""
            + "CREATE TABLE IF NOT EXISTS linkmind_user_api_keys ("
            + " id INTEGER PRIMARY KEY AUTOINCREMENT,"
            + " user_id TEXT NOT NULL,"
            + " api_key TEXT NOT NULL UNIQUE,"
            + " key_hint TEXT NOT NULL,"
            + " remark TEXT NOT NULL DEFAULT '',"
            + " status TEXT NOT NULL DEFAULT 'active',"
            + " created_at INTEGER NOT NULL,"
            + " last_used_at INTEGER,"
            + " revoked_at INTEGER,"
            + " FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE"
            + ")";

    public static void resetSchemaForTests() {
        synchronized (SCHEMA_LOCK) {
            schemaReady = false;
        }
    }

    public void initialize() throws SQLException {
        ensureSchema();
    }

    public boolean userExists(String userId) throws SQLException {
        if (isBlank(userId)) {
            return false;
        }
        try (Connection conn = TokenLinkDataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT 1 FROM users WHERE id = ? LIMIT 1")) {
            ps.setString(1, userId.trim());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public LocalUserApiKey insert(String userId, String apiKey, String keyHint, String remark) throws SQLException {
        ensureSchema();
        long now = System.currentTimeMillis();
        String sql = "INSERT INTO linkmind_user_api_keys"
                + "(user_id, api_key, key_hint, remark, status, created_at) VALUES (?, ?, ?, ?, 'active', ?)";
        try (Connection conn = TokenLinkDataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, userId);
            ps.setString(2, apiKey);
            ps.setString(3, keyHint);
            ps.setString(4, remark);
            ps.setLong(5, now);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException("generated local user API key id is unavailable");
                }
                return LocalUserApiKey.builder()
                        .id(keys.getLong(1))
                        .userId(userId)
                        .apiKey(apiKey)
                        .keyHint(keyHint)
                        .remark(remark)
                        .status("active")
                        .createdAt(now)
                        .build();
            }
        }
    }

    public List<LocalUserApiKey> list(String userId, String status, int page, int pageSize) throws SQLException {
        ensureSchema();
        boolean filterStatus = !isBlank(status) && !"all".equalsIgnoreCase(status);
        String sql = "SELECT id, user_id, key_hint, remark, status, created_at, last_used_at, revoked_at "
                + "FROM linkmind_user_api_keys WHERE user_id = ?"
                + (filterStatus ? " AND status = ?" : "")
                + " ORDER BY id DESC LIMIT ? OFFSET ?";
        List<LocalUserApiKey> result = new ArrayList<LocalUserApiKey>();
        try (Connection conn = TokenLinkDataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            int index = 1;
            ps.setString(index++, userId);
            if (filterStatus) {
                ps.setString(index++, status.toLowerCase());
            }
            ps.setInt(index++, pageSize);
            ps.setInt(index, (page - 1) * pageSize);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapMetadata(rs));
                }
            }
        }
        return result;
    }

    public long count(String userId, String status) throws SQLException {
        ensureSchema();
        boolean filterStatus = !isBlank(status) && !"all".equalsIgnoreCase(status);
        String sql = "SELECT COUNT(1) FROM linkmind_user_api_keys WHERE user_id = ?"
                + (filterStatus ? " AND status = ?" : "");
        try (Connection conn = TokenLinkDataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId);
            if (filterStatus) {
                ps.setString(2, status.toLowerCase());
            }
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getLong(1) : 0L;
            }
        }
    }

    public LocalUserApiKey findActiveByApiKey(String apiKey) throws SQLException {
        LocalUserApiKey key = findByApiKey(apiKey);
        return key != null && key.isActive() ? key : null;
    }

    public LocalUserApiKey findByApiKey(String apiKey) throws SQLException {
        if (isBlank(apiKey)) {
            return null;
        }
        String sql = "SELECT k.id, k.user_id, k.api_key, k.key_hint, k.remark, k.status, k.created_at, "
                + "k.last_used_at, k.revoked_at FROM linkmind_user_api_keys k "
                + "INNER JOIN users u ON u.id = k.user_id "
                + "WHERE k.api_key = ? LIMIT 1";
        try (Connection conn = TokenLinkDataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, apiKey.trim());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapWithKey(rs) : null;
            }
        }
    }

    public LocalUserApiKey findMetadata(String userId, long id) throws SQLException {
        ensureSchema();
        String sql = "SELECT id, user_id, key_hint, remark, status, created_at, last_used_at, revoked_at "
                + "FROM linkmind_user_api_keys WHERE user_id = ? AND id = ? LIMIT 1";
        try (Connection conn = TokenLinkDataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId);
            ps.setLong(2, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapMetadata(rs) : null;
            }
        }
    }

    public boolean updateRemark(String userId, long id, String remark) throws SQLException {
        ensureSchema();
        try (Connection conn = TokenLinkDataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE linkmind_user_api_keys SET remark = ? WHERE user_id = ? AND id = ?")) {
            ps.setString(1, remark);
            ps.setString(2, userId);
            ps.setLong(3, id);
            return ps.executeUpdate() == 1;
        }
    }

    public boolean revoke(String userId, long id) throws SQLException {
        ensureSchema();
        try (Connection conn = TokenLinkDataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE linkmind_user_api_keys SET status = 'revoked', revoked_at = ? "
                             + "WHERE user_id = ? AND id = ? AND status = 'active'")) {
            ps.setLong(1, System.currentTimeMillis());
            ps.setString(2, userId);
            ps.setLong(3, id);
            return ps.executeUpdate() == 1;
        }
    }

    public void touchLastUsed(long id) throws SQLException {
        try (Connection conn = TokenLinkDataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE linkmind_user_api_keys SET last_used_at = ? WHERE id = ? AND status = 'active'")) {
            ps.setLong(1, System.currentTimeMillis());
            ps.setLong(2, id);
            ps.executeUpdate();
        }
    }

    private void ensureSchema() throws SQLException {
        if (schemaReady) {
            return;
        }
        synchronized (SCHEMA_LOCK) {
            if (schemaReady) {
                return;
            }
            try (Connection conn = TokenLinkDataSource.getConnection();
                 Statement statement = conn.createStatement()) {
                statement.executeUpdate(CREATE_TABLE);
                statement.executeUpdate("CREATE INDEX IF NOT EXISTS idx_linkmind_user_api_keys_user_id "
                        + "ON linkmind_user_api_keys(user_id)");
                statement.executeUpdate("CREATE INDEX IF NOT EXISTS idx_linkmind_user_api_keys_status "
                        + "ON linkmind_user_api_keys(status)");
                schemaReady = true;
            }
        }
    }

    private LocalUserApiKey mapMetadata(ResultSet rs) throws SQLException {
        return LocalUserApiKey.builder()
                .id(rs.getLong("id"))
                .userId(rs.getString("user_id"))
                .keyHint(rs.getString("key_hint"))
                .remark(rs.getString("remark"))
                .status(rs.getString("status"))
                .createdAt(nullableLong(rs, "created_at"))
                .lastUsedAt(nullableLong(rs, "last_used_at"))
                .revokedAt(nullableLong(rs, "revoked_at"))
                .build();
    }

    private LocalUserApiKey mapWithKey(ResultSet rs) throws SQLException {
        LocalUserApiKey key = mapMetadata(rs);
        key.setApiKey(rs.getString("api_key"));
        return key;
    }

    private static Long nullableLong(ResultSet rs, String field) throws SQLException {
        long value = rs.getLong(field);
        return rs.wasNull() ? null : value;
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}

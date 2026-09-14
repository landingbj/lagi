package ai.localkey;

import ai.common.db.HikariDS;
import ai.utils.AiGlobal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/** SQLite persistence for box-owned {@code lmk_} credentials. */
final class LocalBoxKeyDao {
    private static final Object SCHEMA_LOCK = new Object();
    private static volatile boolean schemaReady;

    private static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS linkmind_local_api_keys ("
            + " id INTEGER PRIMARY KEY AUTOINCREMENT,"
            + " subject_id TEXT NOT NULL,"
            + " display_name TEXT NOT NULL DEFAULT '',"
            + " api_key TEXT NOT NULL UNIQUE,"
            + " key_hint TEXT NOT NULL,"
            + " remark TEXT NOT NULL DEFAULT '',"
            + " status TEXT NOT NULL DEFAULT 'active',"
            + " created_at INTEGER NOT NULL,"
            + " last_used_at INTEGER,"
            + " revoked_at INTEGER"
            + ")";

    void initialize() throws SQLException {
        if (schemaReady) {
            return;
        }
        synchronized (SCHEMA_LOCK) {
            if (schemaReady) {
                return;
            }
            try (Connection connection = HikariDS.getConnection(AiGlobal.DEFAULT_DB);
                 Statement statement = connection.createStatement()) {
                statement.executeUpdate(CREATE_TABLE);
                statement.executeUpdate("CREATE INDEX IF NOT EXISTS idx_linkmind_local_api_keys_subject "
                        + "ON linkmind_local_api_keys(subject_id)");
                statement.executeUpdate("CREATE INDEX IF NOT EXISTS idx_linkmind_local_api_keys_status "
                        + "ON linkmind_local_api_keys(status)");
                schemaReady = true;
            }
        }
    }

    LocalBoxKey insert(String subjectId, String displayName, String apiKey, String keyHint, String remark)
            throws SQLException {
        initialize();
        long now = System.currentTimeMillis();
        String sql = "INSERT INTO linkmind_local_api_keys "
                + "(subject_id, display_name, api_key, key_hint, remark, status, created_at) "
                + "VALUES (?, ?, ?, ?, ?, 'active', ?)";
        try (Connection connection = HikariDS.getConnection(AiGlobal.DEFAULT_DB);
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, subjectId);
            statement.setString(2, displayName);
            statement.setString(3, apiKey);
            statement.setString(4, keyHint);
            statement.setString(5, remark);
            statement.setLong(6, now);
            statement.executeUpdate();
            try (ResultSet generated = statement.getGeneratedKeys()) {
                if (!generated.next()) {
                    throw new SQLException("generated local key id is unavailable");
                }
                return LocalBoxKey.builder().id(generated.getLong(1)).subjectId(subjectId)
                        .displayName(displayName).apiKey(apiKey).keyHint(keyHint).remark(remark)
                        .status("active").createdAt(now).build();
            }
        }
    }

    LocalBoxKey findByKey(String apiKey, boolean includeSecret) throws SQLException {
        initialize();
        String sql = "SELECT id, subject_id, display_name, api_key, key_hint, remark, status, created_at, "
                + "last_used_at, revoked_at FROM linkmind_local_api_keys WHERE api_key = ? LIMIT 1";
        try (Connection connection = HikariDS.getConnection(AiGlobal.DEFAULT_DB);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, apiKey);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? map(result, includeSecret) : null;
            }
        }
    }

    List<LocalBoxKey> list(String subjectId) throws SQLException {
        initialize();
        boolean scoped = subjectId != null && !subjectId.trim().isEmpty();
        String sql = "SELECT id, subject_id, display_name, key_hint, remark, status, created_at, last_used_at, revoked_at "
                + "FROM linkmind_local_api_keys" + (scoped ? " WHERE subject_id = ?" : "") + " ORDER BY id DESC";
        List<LocalBoxKey> keys = new ArrayList<LocalBoxKey>();
        try (Connection connection = HikariDS.getConnection(AiGlobal.DEFAULT_DB);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            if (scoped) {
                statement.setString(1, subjectId.trim());
            }
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    keys.add(map(result, false));
                }
            }
        }
        return keys;
    }

    boolean revoke(long id) throws SQLException {
        initialize();
        try (Connection connection = HikariDS.getConnection(AiGlobal.DEFAULT_DB);
             PreparedStatement statement = connection.prepareStatement("UPDATE linkmind_local_api_keys "
                     + "SET status = 'revoked', revoked_at = ? WHERE id = ? AND status = 'active'")) {
            statement.setLong(1, System.currentTimeMillis());
            statement.setLong(2, id);
            return statement.executeUpdate() == 1;
        }
    }

    boolean updateRemark(long id, String remark) throws SQLException {
        initialize();
        try (Connection connection = HikariDS.getConnection(AiGlobal.DEFAULT_DB);
             PreparedStatement statement = connection.prepareStatement("UPDATE linkmind_local_api_keys SET remark = ? WHERE id = ?")) {
            statement.setString(1, remark);
            statement.setLong(2, id);
            return statement.executeUpdate() == 1;
        }
    }

    void touch(long id) throws SQLException {
        try (Connection connection = HikariDS.getConnection(AiGlobal.DEFAULT_DB);
             PreparedStatement statement = connection.prepareStatement("UPDATE linkmind_local_api_keys SET last_used_at = ? "
                     + "WHERE id = ? AND status = 'active'")) {
            statement.setLong(1, System.currentTimeMillis());
            statement.setLong(2, id);
            statement.executeUpdate();
        }
    }

    private LocalBoxKey map(ResultSet result, boolean includeSecret) throws SQLException {
        LocalBoxKey.LocalBoxKeyBuilder builder = LocalBoxKey.builder()
                .id(result.getLong("id"))
                .subjectId(result.getString("subject_id"))
                .displayName(result.getString("display_name"))
                .keyHint(result.getString("key_hint"))
                .remark(result.getString("remark"))
                .status(result.getString("status"))
                .createdAt(nullableLong(result, "created_at"))
                .lastUsedAt(nullableLong(result, "last_used_at"))
                .revokedAt(nullableLong(result, "revoked_at"));
        if (includeSecret) {
            builder.apiKey(result.getString("api_key"));
        }
        return builder.build();
    }

    private static Long nullableLong(ResultSet result, String name) throws SQLException {
        long value = result.getLong(name);
        return result.wasNull() ? null : value;
    }
}

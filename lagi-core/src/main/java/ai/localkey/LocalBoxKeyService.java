package ai.localkey;

import java.security.SecureRandom;
import java.sql.SQLException;
import java.util.Base64;
import java.util.List;

/** Lifecycle and authentication service for local box-issued {@code lmk_} keys. */
public class LocalBoxKeyService {
    public static final String PREFIX = "lmk_";
    private static final int KEY_BYTES = 32;
    private static final int MAX_TEXT_LENGTH = 120;
    private static final SecureRandom RANDOM = new SecureRandom();
    private final LocalBoxKeyDao dao = new LocalBoxKeyDao();

    public LocalBoxKey create(String subjectId, String displayName, String remark) throws SQLException {
        String subject = requireText(subjectId, "subjectId");
        String name = normalize(displayName, "displayName");
        String note = normalize(remark, "remark");
        for (int i = 0; i < 3; i++) {
            String key = nextKey();
            try {
                return dao.insert(subject, name, key, hint(key), note);
            } catch (SQLException error) {
                if (!isUniqueViolation(error) || i == 2) {
                    throw error;
                }
            }
        }
        throw new SQLException("unable to generate a unique local key");
    }

    public LocalBoxKey authenticate(String apiKey) throws SQLException {
        if (!isLocalKey(apiKey)) {
            return null;
        }
        LocalBoxKey key = dao.findByKey(apiKey.trim(), true);
        if (key == null || !key.isActive()) {
            return null;
        }
        dao.touch(key.getId());
        return key;
    }

    public List<LocalBoxKey> list(String subjectId) throws SQLException {
        return dao.list(subjectId);
    }

    public boolean revoke(long id) throws SQLException {
        return dao.revoke(id);
    }

    public boolean updateRemark(long id, String remark) throws SQLException {
        return dao.updateRemark(id, normalize(remark, "remark"));
    }

    public static boolean isLocalKey(String apiKey) {
        return apiKey != null && apiKey.startsWith(PREFIX);
    }

    private static String nextKey() {
        byte[] bytes = new byte[KEY_BYTES];
        RANDOM.nextBytes(bytes);
        return PREFIX + Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static String hint(String key) {
        return key.substring(0, PREFIX.length() + 4) + "..." + key.substring(key.length() - 4);
    }

    private static String requireText(String value, String name) {
        String normalized = normalize(value, name);
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException(name + " is required");
        }
        return normalized;
    }

    private static String normalize(String value, String name) {
        String normalized = value == null ? "" : value.trim();
        if (normalized.length() > MAX_TEXT_LENGTH || normalized.indexOf('\u0000') >= 0) {
            throw new IllegalArgumentException(name + " is invalid");
        }
        return normalized;
    }

    private static boolean isUniqueViolation(SQLException error) {
        String message = error.getMessage();
        return message != null && (message.contains("UNIQUE constraint") || message.contains("SQLITE_CONSTRAINT"));
    }
}

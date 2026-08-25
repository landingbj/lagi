package ai.account;

import java.security.SecureRandom;
import java.sql.SQLException;
import java.util.Base64;
import java.util.List;

/** Lifecycle and admission service for local {@code lmk_} user keys. */
public class LocalUserApiKeyService {
    public static final String KEY_PREFIX = "lmk_";
    private static final int KEY_BYTES = 32;
    private static final int MAX_RETRIES = 3;
    private static final int MAX_REMARK_LENGTH = 120;
    private static final SecureRandom RANDOM = new SecureRandom();
    private final LocalUserApiKeyDao dao = new LocalUserApiKeyDao();

    public void initialize() throws SQLException {
        dao.initialize();
    }

    public LocalUserApiKey create(String userId, String remark) throws SQLException, UserKeyException {
        String normalizedUserId = requireUserId(userId);
        String normalizedRemark = normalizeRemark(remark);
        if (!dao.userExists(normalizedUserId)) {
            throw UserKeyException.userNotFound();
        }
        for (int attempt = 0; attempt < MAX_RETRIES; attempt++) {
            String apiKey = newApiKey();
            try {
                return dao.insert(normalizedUserId, apiKey, keyHint(apiKey), normalizedRemark);
            } catch (SQLException e) {
                if (!isUniqueKeyViolation(e) || attempt == MAX_RETRIES - 1) {
                    throw e;
                }
            }
        }
        throw new SQLException("could not generate a unique local user API key");
    }

    public List<LocalUserApiKey> list(String userId, String status, int page, int pageSize)
            throws SQLException, UserKeyException {
        String normalizedUserId = requireUserId(userId);
        validateStatus(status);
        if (!dao.userExists(normalizedUserId)) {
            throw UserKeyException.userNotFound();
        }
        return dao.list(normalizedUserId, normalizedStatus(status), page, pageSize);
    }

    public long count(String userId, String status) throws SQLException, UserKeyException {
        String normalizedUserId = requireUserId(userId);
        validateStatus(status);
        if (!dao.userExists(normalizedUserId)) {
            throw UserKeyException.userNotFound();
        }
        return dao.count(normalizedUserId, normalizedStatus(status));
    }

    public LocalUserApiKey updateRemark(String userId, long keyId, String remark)
            throws SQLException, UserKeyException {
        String normalizedUserId = requireUserId(userId);
        String normalizedRemark = normalizeRemark(remark);
        if (!dao.userExists(normalizedUserId)) {
            throw UserKeyException.userNotFound();
        }
        if (!dao.updateRemark(normalizedUserId, keyId, normalizedRemark)) {
            throw UserKeyException.keyNotFound();
        }
        LocalUserApiKey key = dao.findMetadata(normalizedUserId, keyId);
        if (key == null) {
            throw UserKeyException.keyNotFound();
        }
        return key;
    }

    public LocalUserApiKey revoke(String userId, long keyId) throws SQLException, UserKeyException {
        String normalizedUserId = requireUserId(userId);
        if (!dao.userExists(normalizedUserId)) {
            throw UserKeyException.userNotFound();
        }
        LocalUserApiKey current = dao.findMetadata(normalizedUserId, keyId);
        if (current == null) {
            throw UserKeyException.keyNotFound();
        }
        if (!current.isActive()) {
            throw UserKeyException.keyAlreadyRevoked();
        }
        if (!dao.revoke(normalizedUserId, keyId)) {
            throw UserKeyException.keyAlreadyRevoked();
        }
        LocalUserApiKey revoked = dao.findMetadata(normalizedUserId, keyId);
        if (revoked == null) {
            throw UserKeyException.keyNotFound();
        }
        return revoked;
    }

    /** Returns an active key only when its associated Cuihua user still exists. */
    public LocalUserApiKey authenticate(String apiKey) throws SQLException {
        if (apiKey == null || !apiKey.startsWith(KEY_PREFIX)) {
            return null;
        }
        LocalUserApiKey key = dao.findByApiKey(apiKey);
        if (key == null || !key.isActive()) {
            return null;
        }
        if (key != null && key.getId() != null) {
            dao.touchLastUsed(key.getId());
        }
        return key;
    }

    /** Finds a local key and its state without treating it as an admitted model request. */
    public LocalUserApiKey find(String apiKey) throws SQLException {
        if (!isLocalUserKey(apiKey)) {
            return null;
        }
        return dao.findByApiKey(apiKey);
    }

    public static boolean isLocalUserKey(String apiKey) {
        return apiKey != null && apiKey.startsWith(KEY_PREFIX);
    }

    private static String requireUserId(String userId) throws UserKeyException {
        if (userId == null || userId.trim().isEmpty()) {
            throw UserKeyException.userNotFound();
        }
        return userId.trim();
    }

    private static String normalizeRemark(String remark) throws UserKeyException {
        String value = remark == null ? "" : remark.trim();
        if (value.length() > MAX_REMARK_LENGTH || value.indexOf('\u0000') >= 0) {
            throw UserKeyException.invalidRemark();
        }
        return value;
    }

    private static String normalizedStatus(String status) {
        return status == null || status.trim().isEmpty() ? "all" : status.trim().toLowerCase();
    }

    private static void validateStatus(String status) throws UserKeyException {
        String normalized = normalizedStatus(status);
        if (!"all".equals(normalized) && !"active".equals(normalized) && !"revoked".equals(normalized)) {
            throw UserKeyException.invalidStatus();
        }
    }

    private static String newApiKey() {
        byte[] bytes = new byte[KEY_BYTES];
        RANDOM.nextBytes(bytes);
        return KEY_PREFIX + Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static String keyHint(String apiKey) {
        if (apiKey.length() <= KEY_PREFIX.length() + 8) {
            return KEY_PREFIX + "...";
        }
        return apiKey.substring(0, KEY_PREFIX.length() + 4) + "..."
                + apiKey.substring(apiKey.length() - 4);
    }

    private static boolean isUniqueKeyViolation(SQLException e) {
        String message = e.getMessage();
        return message != null && (message.contains("UNIQUE constraint failed") || message.contains("SQLITE_CONSTRAINT"));
    }

    public static final class UserKeyException extends Exception {
        public enum Reason { USER_NOT_FOUND, USER_KEY_NOT_FOUND, USER_KEY_ALREADY_REVOKED, INVALID_REMARK, INVALID_STATUS }

        private final Reason reason;

        private UserKeyException(Reason reason) {
            super(reason.name());
            this.reason = reason;
        }

        public Reason getReason() {
            return reason;
        }

        private static UserKeyException userNotFound() {
            return new UserKeyException(Reason.USER_NOT_FOUND);
        }

        private static UserKeyException keyNotFound() {
            return new UserKeyException(Reason.USER_KEY_NOT_FOUND);
        }

        private static UserKeyException keyAlreadyRevoked() {
            return new UserKeyException(Reason.USER_KEY_ALREADY_REVOKED);
        }

        private static UserKeyException invalidRemark() {
            return new UserKeyException(Reason.INVALID_REMARK);
        }

        private static UserKeyException invalidStatus() {
            return new UserKeyException(Reason.INVALID_STATUS);
        }
    }
}

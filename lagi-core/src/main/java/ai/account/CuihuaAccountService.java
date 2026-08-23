package ai.account;

import ai.common.db.CuihuaDataSource;
import ai.config.pojo.CuihuaConfig;
import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;

import java.security.SecureRandom;
import java.sql.SQLException;
import java.util.Base64;

/** Authenticates through the read-only Cuihua user view without copying credentials into LinkMind. */
public class CuihuaAccountService {
    private static final Object INITIALIZATION_LOCK = new Object();
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int SESSION_BYTES = 32;
    private static volatile boolean initialized;
    private final CuihuaAccountDao accountDao = new CuihuaAccountDao();

    public static void initialize(CuihuaConfig config) throws SQLException {
        synchronized (INITIALIZATION_LOCK) {
            CuihuaDataSource.initialize(config);
            if (initialized) {
                return;
            }
            new CuihuaAccountDao().initialize();
            initialized = true;
        }
    }

    public static void resetForTests() {
        synchronized (INITIALIZATION_LOCK) {
            initialized = false;
            CuihuaAccountDao.resetSchemaForTests();
            CuihuaDataSource.resetForTests();
        }
    }

    public LoginResult login(String usernameOrEmail, String password, long sessionLifetimeMillis) throws SQLException {
        return login(usernameOrEmail, password, sessionLifetimeMillis, false);
    }

    /**
     * Creates a LinkMind console session only for a shared Cuihua
     * {@code system_admin}. Regular Cuihua users use the business system and
     * client API keys instead of logging into LinkMind's management console.
     */
    public LoginResult loginSystemAdmin(String usernameOrEmail, String password, long sessionLifetimeMillis) throws SQLException {
        return login(usernameOrEmail, password, sessionLifetimeMillis, true);
    }

    private LoginResult login(String usernameOrEmail, String password, long sessionLifetimeMillis,
                              boolean requireSystemAdmin) throws SQLException {
        if (isBlank(usernameOrEmail) || password == null || password.isEmpty()) {
            return null;
        }
        CuihuaUser user = accountDao.findActiveUserByLogin(usernameOrEmail);
        if (user == null || !verifyPassword(user.getPasswordHash(), password)) {
            return null;
        }
        if (requireSystemAdmin && !user.isSystemAdmin()) {
            return null;
        }
        String sessionToken = newSessionToken();
        long expiresAt = System.currentTimeMillis() + sessionLifetimeMillis;
        accountDao.insertLoginSession(user.getUserId(), CuihuaAccountDao.sha256(sessionToken), expiresAt);
        return new LoginResult(user, sessionToken, expiresAt);
    }

    public CuihuaUser resolveSession(String sessionToken) throws SQLException {
        if (isBlank(sessionToken)) {
            return null;
        }
        String sessionHash = CuihuaAccountDao.sha256(sessionToken.trim());
        CuihuaUser user = accountDao.findActiveUserBySessionHash(sessionHash);
        if (user != null) {
            accountDao.touchLoginSession(sessionHash);
        }
        return user;
    }

    public void revokeSession(String sessionToken) throws SQLException {
        if (!isBlank(sessionToken)) {
            accountDao.deleteLoginSession(CuihuaAccountDao.sha256(sessionToken.trim()));
        }
    }

    public boolean isSystemAdmin(CuihuaUser user) {
        return user != null && user.isSystemAdmin();
    }

    private boolean verifyPassword(String encodedHash, String password) {
        if (isBlank(encodedHash) || !encodedHash.startsWith("$argon2id$")) {
            return false;
        }
        char[] secret = password.toCharArray();
        Argon2 argon2 = Argon2Factory.create(Argon2Factory.Argon2Types.ARGON2id);
        try {
            return argon2.verify(encodedHash, secret);
        } catch (RuntimeException ignored) {
            return false;
        } finally {
            argon2.wipeArray(secret);
        }
    }

    private String newSessionToken() {
        byte[] raw = new byte[SESSION_BYTES];
        RANDOM.nextBytes(raw);
        return "lms_" + Base64.getUrlEncoder().withoutPadding().encodeToString(raw);
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    public static final class LoginResult {
        private final CuihuaUser user;
        private final String sessionToken;
        private final long expiresAt;

        private LoginResult(CuihuaUser user, String sessionToken, long expiresAt) {
            this.user = user;
            this.sessionToken = sessionToken;
            this.expiresAt = expiresAt;
        }

        public CuihuaUser getUser() {
            return user;
        }

        public String getSessionToken() {
            return sessionToken;
        }

        public long getExpiresAt() {
            return expiresAt;
        }
    }
}

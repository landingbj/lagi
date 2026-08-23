package ai.account;

import ai.common.db.CuihuaDataSource;
import ai.common.db.HikariDS;
import ai.config.pojo.CuihuaConfig;
import ai.llm.dao.TokenStatisticsDao;
import ai.llm.pojo.TokenStatisticsRange;
import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CuihuaAccountIntegrationTest {
    private static final String USER_ID = "cuihua-user-1";
    private static final String SYSTEM_ADMIN_ID = "cuihua-system-admin-1";
    private static final String PASSWORD = "correct-horse-battery-staple";

    private static String originalJdbcUrl;
    private static Path cuihuaDatabase;
    private static String passwordHash;

    @BeforeAll
    static void createSeparateDatabases() throws Exception {
        Path dataDir = Files.createTempDirectory("linkmind-cuihua-test-");
        Path linkMindDatabase = dataDir.resolve("linkmind.db").toAbsolutePath();
        cuihuaDatabase = dataDir.resolve("cuihua.db").toAbsolutePath();

        originalJdbcUrl = HikariDS.getJdbcUrlForTests();
        HikariDS.setJdbcUrlForTests(sqliteJdbcUrl(linkMindDatabase));
        CuihuaAccountService.resetForTests();
        CuihuaAccountDao.resetSchemaForTests();
        createLinkMindDatabase();
        createCuihuaDatabase();

        CuihuaAccountService.initialize(cuihuaConfig(true));
    }

    @AfterAll
    static void restoreDataSources() {
        CuihuaAccountService.resetForTests();
        if (originalJdbcUrl != null) {
            HikariDS.setJdbcUrlForTests(originalJdbcUrl);
        }
    }

    @Test
    void authenticatesFromCuihuaAndKeepsLinkMindDataLocal() throws Exception {
        CuihuaAccountService accountService = new CuihuaAccountService();
        CuihuaAccountService.LoginResult login = accountService.login(
                "cuihua@example.test", PASSWORD, 60_000L);
        assertNotNull(login);
        assertEquals(USER_ID, login.getUser().getUserId());
        assertNull(accountService.login("cuihua", "wrong-password", 60_000L));
        assertNull(accountService.loginSystemAdmin("cuihua", PASSWORD, 60_000L));

        CuihuaAccountService.LoginResult systemAdmin = accountService.loginSystemAdmin(
                "system-admin", PASSWORD, 60_000L);
        assertNotNull(systemAdmin);
        assertEquals(SYSTEM_ADMIN_ID, systemAdmin.getUser().getUserId());
        assertEquals("system_admin", systemAdmin.getUser().getRole());
        assertEquals(USER_ID, accountService.resolveSession(login.getSessionToken()).getUserId());

        LinkMindClientTokenService tokenService = new LinkMindClientTokenService();
        LinkMindClientToken clientToken = tokenService.issue(USER_ID, "integration-test", null);
        assertNotNull(clientToken.getToken());
        assertTrue(tokenService.authenticate(clientToken.getToken()).isAuthenticated());

        deleteCuihuaUser(USER_ID);
        LinkMindClientTokenService.AuthenticationResult removed = tokenService.authenticate(clientToken.getToken());
        assertTrue(removed.isTokenMatched());
        assertFalse(removed.isAuthenticated());
        assertNull(accountService.resolveSession(login.getSessionToken()));

        insertCuihuaUser(USER_ID, "cuihua", "cuihua@example.test", "Cuihua User", "user");
        assertTrue(tokenService.authenticate(clientToken.getToken()).isAuthenticated());
        assertEquals(USER_ID, accountService.resolveSession(login.getSessionToken()).getUserId());

        LinkMindClientToken updated = tokenService.update(clientToken.getId(), USER_ID,
                "renamed", false, null, false);
        assertNotNull(updated);
        assertEquals("renamed", updated.getName());
        assertFalse(updated.isEnabled());
        assertFalse(tokenService.authenticate(clientToken.getToken()).isAuthenticated());
        assertTrue(tokenService.delete(clientToken.getId(), USER_ID));
        assertTrue(tokenService.list(USER_ID).isEmpty());

        TokenStatisticsDao statisticsDao = new TokenStatisticsDao();
        statisticsDao.insert(11L, 7L, 18L, 0L, "Test", "test-model", "session-1", USER_ID);
        assertEquals(18L, statisticsDao.summarize(TokenStatisticsRange.ALL, USER_ID).getTotalTokensConsumed());

        assertLinkMindSchemaHasNoCuihuaObjects();
        assertThrows(SQLException.class, () -> {
            try (Connection conn = CuihuaDataSource.getConnection();
                 Statement statement = conn.createStatement()) {
                statement.execute("CREATE TABLE linkmind_must_not_write_here(id INTEGER)");
            }
        });

        accountService.revokeSession(login.getSessionToken());
        assertNull(accountService.resolveSession(login.getSessionToken()));

        assertThrows(IllegalStateException.class,
                () -> CuihuaAccountService.initialize(cuihuaConfig(false)));

        deleteCuihuaUser(USER_ID);
        deleteCuihuaUser(SYSTEM_ADMIN_ID);
        CuihuaAccountService.resetForTests();
        CuihuaAccountService.initialize(cuihuaConfig(true));
    }

    private static void createLinkMindDatabase() throws SQLException {
        try (Connection conn = HikariDS.getConnection("saas");
             Statement statement = conn.createStatement()) {
            statement.execute("CREATE TABLE llm_token_statistics ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT, created_at INTEGER NOT NULL, "
                    + "prompt_tokens INTEGER NOT NULL, completion_tokens INTEGER NOT NULL, "
                    + "total_tokens INTEGER NOT NULL, saved_tokens INTEGER NOT NULL, "
                    + "provider TEXT, model TEXT, session_id TEXT, user_id TEXT)");
        }
    }

    private static void createCuihuaDatabase() throws Exception {
        Argon2 argon2 = Argon2Factory.create(Argon2Factory.Argon2Types.ARGON2id);
        char[] password = PASSWORD.toCharArray();
        try {
            passwordHash = argon2.hash(3, 65536, 4, password);
        } finally {
            argon2.wipeArray(password);
        }

        try (Connection conn = DriverManager.getConnection(sqliteJdbcUrl(cuihuaDatabase));
             Statement statement = conn.createStatement()) {
            statement.execute("PRAGMA journal_mode=WAL");
            statement.execute("CREATE TABLE users ("
                    + "id VARCHAR(64) NOT NULL PRIMARY KEY, username VARCHAR(128) NOT NULL, "
                    + "email VARCHAR(320) NOT NULL, display_name VARCHAR(120) NOT NULL, "
                    + "password_hash TEXT NOT NULL, role VARCHAR(32) NOT NULL DEFAULT 'user', "
                    + "status VARCHAR(32) NOT NULL DEFAULT 'active', first_login BOOLEAN NOT NULL DEFAULT 1, "
                    + "failed_attempts INTEGER NOT NULL DEFAULT 0, locked_until DATETIME, created_at DATETIME NOT NULL)");
            statement.execute("CREATE UNIQUE INDEX ix_users_username ON users(username)");
            statement.execute("CREATE UNIQUE INDEX ix_users_email ON users(email)");
            statement.execute("CREATE TABLE roles (id TEXT PRIMARY KEY, code TEXT NOT NULL UNIQUE)");
            statement.execute("CREATE TABLE user_roles (user_id TEXT NOT NULL, role_id TEXT NOT NULL)");
            statement.execute("INSERT INTO roles(id, code) VALUES ('role-admin', 'system_admin')");
            statement.execute("CREATE VIEW v_linkmind_users AS "
                    + "SELECT u.id AS user_id, u.username, u.email, u.display_name, u.password_hash, "
                    + "CASE WHEN EXISTS (SELECT 1 FROM user_roles ur JOIN roles r ON r.id = ur.role_id "
                    + "WHERE ur.user_id = u.id AND r.code = 'system_admin') "
                    + "THEN 'system_admin' ELSE 'user' END AS role, "
                    + "u.status, u.locked_until, "
                    + "CAST(strftime('%s', u.created_at) AS INTEGER) * 1000 AS created_at "
                    + "FROM users u WHERE LOWER(u.status) = 'active'");
        }
        insertCuihuaUser(USER_ID, "cuihua", "cuihua@example.test", "Cuihua User", "user");
        insertCuihuaUser(SYSTEM_ADMIN_ID, "system-admin", "admin@example.test", "System Admin", "admin");
        try (Connection conn = DriverManager.getConnection(sqliteJdbcUrl(cuihuaDatabase));
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO user_roles(user_id, role_id) VALUES (?, 'role-admin')")) {
            ps.setString(1, SYSTEM_ADMIN_ID);
            ps.executeUpdate();
        }
    }

    private static void insertCuihuaUser(String id, String username, String email,
                                         String displayName, String sourceRole) throws SQLException {
        try (Connection conn = DriverManager.getConnection(sqliteJdbcUrl(cuihuaDatabase));
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO users(id, username, email, display_name, password_hash, role, status, created_at) "
                             + "VALUES (?, ?, ?, ?, ?, ?, 'active', datetime('now'))")) {
            ps.setString(1, id);
            ps.setString(2, username);
            ps.setString(3, email);
            ps.setString(4, displayName);
            ps.setString(5, passwordHash);
            ps.setString(6, sourceRole);
            ps.executeUpdate();
        }
    }

    private static void deleteCuihuaUser(String userId) throws SQLException {
        try (Connection conn = DriverManager.getConnection(sqliteJdbcUrl(cuihuaDatabase));
             PreparedStatement ps = conn.prepareStatement("DELETE FROM users WHERE id = ?")) {
            ps.setString(1, userId);
            ps.executeUpdate();
        }
    }

    private static void assertLinkMindSchemaHasNoCuihuaObjects() throws SQLException {
        try (Connection conn = HikariDS.getConnection("saas");
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT COUNT(*) FROM sqlite_master WHERE name IN "
                             + "('users', 'linkmind_user_view', 'linkmind_user_auth_view', "
                             + "'linkmind_user_token_usage_view')");
             ResultSet rs = ps.executeQuery()) {
            assertTrue(rs.next());
            assertEquals(0, rs.getInt(1));
        }
    }

    private static String sqliteJdbcUrl(Path path) {
        return "jdbc:sqlite:" + path.toString().replace('\\', '/');
    }

    private static CuihuaConfig cuihuaConfig(boolean enabled) {
        CuihuaConfig config = new CuihuaConfig();
        config.setEnabled(enabled);
        config.setDatabasePath(cuihuaDatabase.toString());
        config.setUserView("v_linkmind_users");
        return config;
    }
}

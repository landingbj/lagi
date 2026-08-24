package ai.account;

import ai.common.db.HikariDS;
import ai.common.db.TokenLinkDataSource;
import ai.config.pojo.TokenLinkConfig;
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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CuihuaAccountIntegrationTest {
    private static final String USER_ID = "cuihua-user-1";
    private static final String SYSTEM_ADMIN_ID = "cuihua-system-admin-1";
    private static final String PASSWORD = "correct-horse-battery-staple";

    private static String originalJdbcUrl;
    private static String passwordHash;

    @BeforeAll
    static void createSeparateDatabases() throws Exception {
        Path dataDir = Files.createTempDirectory("linkmind-tokenlink-test-");
        originalJdbcUrl = HikariDS.getJdbcUrlForTests();
        HikariDS.setJdbcUrlForTests(sqliteJdbcUrl(dataDir.resolve("saas.db")));

        CuihuaAccountService.resetForTests();
        CuihuaAccountDao.resetSchemaForTests();
        TokenLinkConfig config = new TokenLinkConfig();
        config.setDatabasePath(dataDir.resolve("tokenlink.db").toAbsolutePath().toString());
        CuihuaAccountService.initialize(config);
        createLinkMindDatabase();
        createCuihuaUsers();
    }

    @AfterAll
    static void restoreDataSources() {
        CuihuaAccountService.resetForTests();
        if (originalJdbcUrl != null) {
            HikariDS.setJdbcUrlForTests(originalJdbcUrl);
        }
    }

    @Test
    void usesTokenLinkUsersAndKeepsTheSharedDatabaseUserOnly() throws Exception {
        CuihuaAccountService accountService = new CuihuaAccountService();
        CuihuaAccountService.LoginResult login = accountService.login("cuihua", PASSWORD, 60_000L);
        assertNotNull(login);
        assertEquals(USER_ID, login.getUser().getUserId());
        assertNull(accountService.login("cuihua", "wrong-password", 60_000L));
        assertNull(accountService.loginSystemAdmin("cuihua", PASSWORD, 60_000L));

        CuihuaAccountService.LoginResult systemAdmin = accountService.loginSystemAdminBySso(
                SYSTEM_ADMIN_ID, 60_000L);
        assertNotNull(systemAdmin);
        assertEquals(SYSTEM_ADMIN_ID, systemAdmin.getUser().getUserId());
        assertEquals("system_admin", systemAdmin.getUser().getRole());
        assertEquals(USER_ID, accountService.resolveSession(login.getSessionToken()).getUserId());
        assertNull(accountService.loginSystemAdminBySso(USER_ID, 60_000L));

        setUserLoginStatus(SYSTEM_ADMIN_ID, "logged_out");
        assertNull(accountService.loginSystemAdminBySso(SYSTEM_ADMIN_ID, 60_000L));
        setUserLoginStatus(SYSTEM_ADMIN_ID, "logged_in");

        deleteUser(USER_ID);
        assertNull(accountService.resolveSession(login.getSessionToken()));

        insertUser(USER_ID, "user", "cuihua", "logged_in");
        assertEquals(USER_ID, accountService.resolveSession(login.getSessionToken()).getUserId());

        TokenStatisticsDao statisticsDao = new TokenStatisticsDao();
        statisticsDao.insert(11L, 7L, 18L, 0L, "Test", "test-model", "session-1", USER_ID);
        assertEquals(18L, statisticsDao.summarize(TokenStatisticsRange.ALL, USER_ID).getTotalTokensConsumed());

        assertTokenLinkSchema();
        assertLocalDatabaseHasNoTokenLinkKeyTable();

        accountService.revokeSession(login.getSessionToken());
        assertNull(accountService.resolveSession(login.getSessionToken()));
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

    private static void createCuihuaUsers() throws Exception {
        Argon2 argon2 = Argon2Factory.create(Argon2Factory.Argon2Types.ARGON2id);
        char[] password = PASSWORD.toCharArray();
        try {
            passwordHash = argon2.hash(3, 65536, 4, password);
        } finally {
            argon2.wipeArray(password);
        }
        insertUser(USER_ID, "user", "cuihua", "logged_in");
        insertUser(SYSTEM_ADMIN_ID, "system_admin", "system-admin", "logged_in");
    }

    private static void insertUser(String id, String role, String username, String loginStatus) throws SQLException {
        try (Connection conn = TokenLinkDataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO users(id, role, username, password_hash, user_login_status) VALUES (?, ?, ?, ?, ?)")) {
            ps.setString(1, id);
            ps.setString(2, role);
            ps.setString(3, username);
            ps.setString(4, passwordHash);
            ps.setString(5, loginStatus);
            ps.executeUpdate();
        }
    }

    private static void setUserLoginStatus(String userId, String status) throws SQLException {
        try (Connection conn = TokenLinkDataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement("UPDATE users SET user_login_status = ? WHERE id = ?")) {
            ps.setString(1, status);
            ps.setString(2, userId);
            ps.executeUpdate();
        }
    }

    private static void deleteUser(String userId) throws SQLException {
        try (Connection conn = TokenLinkDataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM users WHERE id = ?")) {
            ps.setString(1, userId);
            ps.executeUpdate();
        }
    }

    private static void assertTokenLinkSchema() throws SQLException {
        try (Connection conn = TokenLinkDataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT COUNT(*) FROM sqlite_master WHERE name = 'users'");
             ResultSet rs = ps.executeQuery()) {
            assertTrue(rs.next());
            assertEquals(1, rs.getInt(1));
        }
    }

    private static void assertLocalDatabaseHasNoTokenLinkKeyTable() throws SQLException {
        try (Connection conn = HikariDS.getConnection("saas");
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT COUNT(*) FROM sqlite_master WHERE name = 'user_api_key'");
             ResultSet rs = ps.executeQuery()) {
            assertTrue(rs.next());
            assertEquals(0, rs.getInt(1));
        }
    }

    private static String sqliteJdbcUrl(Path path) {
        return "jdbc:sqlite:" + path.toAbsolutePath().toString().replace('\\', '/');
    }
}

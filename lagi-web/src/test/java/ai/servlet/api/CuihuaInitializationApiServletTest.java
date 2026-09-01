package ai.servlet.api;

import ai.account.CuihuaAccountDao;
import ai.account.CuihuaAccountService;
import ai.account.LocalUserApiKey;
import ai.account.LocalUserApiKeyService;
import ai.common.db.HikariDS;
import ai.config.pojo.TokenLinkConfig;
import ai.llm.dao.ModelPriceDao;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CuihuaInitializationApiServletTest {
    private static final String SYSTEM_ADMIN_ID = "cuihua-system-admin-initializer";
    private static final String ADMIN_ID = "cuihua-admin-initializer";
    private static String originalJdbcUrl;

    @BeforeAll
    static void createSeparateDatabases() throws Exception {
        Path dataDir = Files.createTempDirectory("linkmind-initialization-api-test-");
        originalJdbcUrl = HikariDS.getJdbcUrlForTests();
        HikariDS.setJdbcUrlForTests(sqliteJdbcUrl(dataDir.resolve("saas.db")));

        CuihuaAccountService.resetForTests();
        CuihuaAccountDao.resetSchemaForTests();
        ModelPriceDao.resetSchemaForTests();
        TokenLinkConfig config = new TokenLinkConfig();
        config.setDatabasePath(dataDir.resolve("tokenlink.db").toAbsolutePath().toString());
        CuihuaAccountService.initialize(config);
        createStatisticsTable();
        insertUser(SYSTEM_ADMIN_ID, "system_admin", "system-initializer");
        insertUser(ADMIN_ID, "admin", "admin-initializer");
    }

    @AfterAll
    static void restoreDataSources() {
        CuihuaAccountService.resetForTests();
        ModelPriceDao.resetSchemaForTests();
        if (originalJdbcUrl != null) {
            HikariDS.setJdbcUrlForTests(originalJdbcUrl);
        }
    }

    @BeforeEach
    void clearInitializationFixtures() throws Exception {
        try (Connection conn = HikariDS.getConnection("saas");
             Statement statement = conn.createStatement()) {
            statement.executeUpdate("DELETE FROM llm_token_statistics");
            statement.executeUpdate("DELETE FROM linkmind_login_session");
        }
        try (Connection conn = ai.common.db.TokenLinkDataSource.getConnection();
             Statement statement = conn.createStatement()) {
            statement.executeUpdate("DELETE FROM linkmind_user_api_keys");
            statement.executeUpdate("UPDATE users SET user_login_status = 'logged_in'");
        }
    }

    @Test
    void loggedInSystemAdministratorCanInitializeUsageAndLinkMindSessions() throws Exception {
        insertStatistics("initialization-usage-1", 18L);
        insertStatistics("initialization-usage-2", 32L);
        CuihuaAccountService accountService = new CuihuaAccountService();
        CuihuaAccountService.LoginResult firstSession = accountService.loginSystemAdminBySso(
                SYSTEM_ADMIN_ID, 60_000L);
        CuihuaAccountService.LoginResult secondSession = accountService.loginSystemAdminBySso(
                SYSTEM_ADMIN_ID, 60_000L);
        LocalUserApiKeyService keyService = new LocalUserApiKeyService();
        LocalUserApiKey localKey = keyService.create(SYSTEM_ADMIN_ID, "preserved-by-initialization");

        ResponseCapture response = invoke("POST", "/v1/integration/initialize", "172.18.0.5",
                Collections.singletonMap(TokenUsageAuthorizationService.CUIHUA_ACTOR_HEADER, SYSTEM_ADMIN_ID),
                "{\"confirmation\":\"RESET_USAGE_AND_SESSIONS\"}");

        assertEquals(HttpServletResponse.SC_OK, response.status);
        JsonObject body = JsonParser.parseString(response.body()).getAsJsonObject();
        assertEquals("success", body.get("status").getAsString());
        JsonObject data = body.getAsJsonObject("data");
        assertEquals(2, data.get("deletedTokenStatistics").getAsInt());
        assertEquals(2, data.get("deletedLoginSessions").getAsInt());
        assertTrue(data.get("initializedAt").getAsLong() > 0L);

        assertNull(accountService.resolveSession(firstSession.getSessionToken()));
        assertNull(accountService.resolveSession(secondSession.getSessionToken()));
        assertNotNull(keyService.authenticate(localKey.getApiKey()));
        assertNotNull(accountService.loginSystemAdminBySso(SYSTEM_ADMIN_ID, 60_000L));

        ResponseCapture usage = invoke("GET", "/v1/integration/token-usage/overview", "172.18.0.5",
                Collections.singletonMap(TokenUsageAuthorizationService.CUIHUA_ACTOR_HEADER, SYSTEM_ADMIN_ID), "");
        JsonObject usageData = JsonParser.parseString(usage.body()).getAsJsonObject().getAsJsonObject("data");
        assertEquals(0L, usageData.get("totalTokens").getAsLong());
        assertEquals(0L, usageData.get("recordCount").getAsLong());
    }

    @Test
    void malformedConfirmationDoesNotInitializeData() throws Exception {
        insertStatistics("malformed-confirmation", 18L);
        CuihuaAccountService accountService = new CuihuaAccountService();
        CuihuaAccountService.LoginResult session = accountService.loginSystemAdminBySso(
                SYSTEM_ADMIN_ID, 60_000L);

        ResponseCapture response = invoke("POST", "/v1/integration/initialize", "172.18.0.5",
                Collections.singletonMap(TokenUsageAuthorizationService.CUIHUA_ACTOR_HEADER, SYSTEM_ADMIN_ID), "{");

        assertEquals(HttpServletResponse.SC_BAD_REQUEST, response.status);
        JsonObject body = JsonParser.parseString(response.body()).getAsJsonObject();
        assertEquals("INVALID_INITIALIZATION_CONFIRMATION", body.get("code").getAsString());
        assertNotNull(accountService.resolveSession(session.getSessionToken()));

        ResponseCapture usage = invoke("GET", "/v1/integration/token-usage/overview", "172.18.0.5",
                Collections.singletonMap(TokenUsageAuthorizationService.CUIHUA_ACTOR_HEADER, SYSTEM_ADMIN_ID), "");
        JsonObject usageData = JsonParser.parseString(usage.body()).getAsJsonObject().getAsJsonObject("data");
        assertEquals(18L, usageData.get("totalTokens").getAsLong());
        assertEquals(1L, usageData.get("recordCount").getAsLong());
    }

    @Test
    void initializationRejectsEveryNonPostMethodWithJsonError() throws Exception {
        ResponseCapture response = invoke("DELETE", "/v1/integration/initialize", "172.18.0.5",
                Collections.singletonMap(TokenUsageAuthorizationService.CUIHUA_ACTOR_HEADER, SYSTEM_ADMIN_ID), "");

        assertEquals(HttpServletResponse.SC_METHOD_NOT_ALLOWED, response.status);
        JsonObject body = JsonParser.parseString(response.body()).getAsJsonObject();
        assertEquals("METHOD_NOT_ALLOWED", body.get("code").getAsString());
    }

    @Test
    void initializationIsIdempotentWhenDataIsAlreadyEmpty() throws Exception {
        ResponseCapture first = initializeAs(SYSTEM_ADMIN_ID, "172.18.0.5");
        ResponseCapture second = initializeAs(SYSTEM_ADMIN_ID, "172.18.0.5");

        assertEquals(HttpServletResponse.SC_OK, first.status);
        assertEquals(HttpServletResponse.SC_OK, second.status);
        JsonObject data = JsonParser.parseString(second.body()).getAsJsonObject().getAsJsonObject("data");
        assertEquals(0, data.get("deletedTokenStatistics").getAsInt());
        assertEquals(0, data.get("deletedLoginSessions").getAsInt());
    }

    @Test
    void initializationRequiresALoggedInSystemAdministrator() throws Exception {
        insertStatistics("authorization-preserved", 18L);

        ResponseCapture admin = initializeAs(ADMIN_ID, "172.18.0.5");
        setUserLoginStatus(SYSTEM_ADMIN_ID, "logged_out");
        ResponseCapture loggedOutSystemAdmin = initializeAs(SYSTEM_ADMIN_ID, "172.18.0.5");
        setUserLoginStatus(SYSTEM_ADMIN_ID, "logged_in");

        assertEquals(HttpServletResponse.SC_FORBIDDEN, admin.status);
        assertEquals("INITIALIZATION_ACCESS_DENIED", errorCode(admin));
        assertEquals(HttpServletResponse.SC_FORBIDDEN, loggedOutSystemAdmin.status);
        assertEquals("INITIALIZATION_ACCESS_DENIED", errorCode(loggedOutSystemAdmin));
        assertEquals(18L, currentTotalTokens());
    }

    @Test
    void initializationRequiresActorHeaderAndBoxInternalNetwork() throws Exception {
        ResponseCapture missingActor = invoke("POST", "/v1/integration/initialize", "172.18.0.5",
                Collections.emptyMap(), "{\"confirmation\":\"RESET_USAGE_AND_SESSIONS\"}");
        ResponseCapture external = initializeAs(SYSTEM_ADMIN_ID, "192.168.88.20");

        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, missingActor.status);
        assertEquals("CUIHUA_ACTOR_REQUIRED", errorCode(missingActor));
        assertEquals(HttpServletResponse.SC_FORBIDDEN, external.status);
        assertEquals("INTERNAL_NETWORK_REQUIRED", errorCode(external));
    }

    @Test
    void databaseFailureRollsBackStatisticsDeletion() throws Exception {
        insertStatistics("rollback-preserved", 18L);
        dropLoginSessionTable();
        try {
            ResponseCapture response = initializeAs(SYSTEM_ADMIN_ID, "172.18.0.5");

            assertEquals(HttpServletResponse.SC_SERVICE_UNAVAILABLE, response.status);
            assertEquals("LOCAL_DATABASE_UNAVAILABLE", errorCode(response));
            assertEquals(18L, currentTotalTokens());
        } finally {
            createLoginSessionTable();
        }
    }

    private static ResponseCapture initializeAs(String actorId, String remoteAddress) throws Exception {
        return invoke("POST", "/v1/integration/initialize", remoteAddress,
                Collections.singletonMap(TokenUsageAuthorizationService.CUIHUA_ACTOR_HEADER, actorId),
                "{\"confirmation\":\"RESET_USAGE_AND_SESSIONS\"}");
    }

    private static String errorCode(ResponseCapture response) {
        return JsonParser.parseString(response.body()).getAsJsonObject().get("code").getAsString();
    }

    private static long currentTotalTokens() throws Exception {
        ResponseCapture usage = invoke("GET", "/v1/integration/token-usage/overview", "172.18.0.5",
                Collections.singletonMap(TokenUsageAuthorizationService.CUIHUA_ACTOR_HEADER, SYSTEM_ADMIN_ID), "");
        return JsonParser.parseString(usage.body()).getAsJsonObject()
                .getAsJsonObject("data").get("totalTokens").getAsLong();
    }

    private static ResponseCapture invoke(String method, String path, String remoteAddress,
                                          Map<String, String> headers, String requestBody) throws Exception {
        byte[] bytes = requestBody.getBytes(StandardCharsets.UTF_8);
        HttpServletRequest request = (HttpServletRequest) Proxy.newProxyInstance(
                CuihuaInitializationApiServletTest.class.getClassLoader(),
                new Class<?>[]{HttpServletRequest.class},
                (proxy, invoked, args) -> {
                    switch (invoked.getName()) {
                        case "getMethod":
                            return method;
                        case "getRequestURI":
                            return path;
                        case "getContextPath":
                            return "";
                        case "getRemoteAddr":
                            return remoteAddress;
                        case "getHeader":
                            return headers.get(String.valueOf(args[0]));
                        case "getInputStream":
                            return inputStream(bytes);
                        default:
                            return defaultValue(invoked.getReturnType());
                    }
                });
        ResponseCapture capture = new ResponseCapture();
        HttpServletResponse response = (HttpServletResponse) Proxy.newProxyInstance(
                CuihuaInitializationApiServletTest.class.getClassLoader(),
                new Class<?>[]{HttpServletResponse.class},
                (proxy, invoked, args) -> {
                    switch (invoked.getName()) {
                        case "setStatus":
                            capture.status = (Integer) args[0];
                            return null;
                        case "getStatus":
                            return capture.status;
                        case "getWriter":
                            return capture.writer;
                        default:
                            return defaultValue(invoked.getReturnType());
                    }
                });
        new CuihuaIntegrationApiServlet().service(request, response);
        capture.writer.flush();
        return capture;
    }

    private static ServletInputStream inputStream(byte[] bytes) {
        ByteArrayInputStream input = new ByteArrayInputStream(bytes);
        return new ServletInputStream() {
            @Override
            public boolean isFinished() {
                return input.available() == 0;
            }

            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public void setReadListener(ReadListener readListener) {
            }

            @Override
            public int read() {
                return input.read();
            }
        };
    }

    private static Object defaultValue(Class<?> type) {
        if (!type.isPrimitive()) {
            return null;
        }
        if (type == boolean.class) {
            return false;
        }
        if (type == byte.class) {
            return (byte) 0;
        }
        if (type == short.class) {
            return (short) 0;
        }
        if (type == int.class) {
            return 0;
        }
        if (type == long.class) {
            return 0L;
        }
        if (type == float.class) {
            return 0F;
        }
        if (type == double.class) {
            return 0D;
        }
        if (type == char.class) {
            return '\0';
        }
        return null;
    }

    private static void createStatisticsTable() throws Exception {
        try (Connection conn = HikariDS.getConnection("saas");
             Statement statement = conn.createStatement()) {
            statement.execute("CREATE TABLE IF NOT EXISTS llm_token_statistics ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT, created_at INTEGER NOT NULL, "
                    + "prompt_tokens INTEGER NOT NULL, completion_tokens INTEGER NOT NULL, "
                    + "total_tokens INTEGER NOT NULL, saved_tokens INTEGER NOT NULL, "
                    + "provider TEXT, model TEXT, session_id TEXT, user_id TEXT, user_api_key_id INTEGER, "
                    + "input_cost DECIMAL(20,8), output_cost DECIMAL(20,8), total_cost DECIMAL(20,8), "
                    + "currency TEXT, price_source TEXT)");
        }
    }

    private static void insertStatistics(String sessionId, long totalTokens) throws Exception {
        try (Connection conn = HikariDS.getConnection("saas");
             PreparedStatement ps = conn.prepareStatement("INSERT INTO llm_token_statistics "
                     + "(created_at, prompt_tokens, completion_tokens, total_tokens, saved_tokens, session_id, user_id) "
                     + "VALUES (?, ?, ?, ?, ?, ?, ?)")) {
            ps.setLong(1, System.currentTimeMillis());
            ps.setLong(2, totalTokens - 1L);
            ps.setLong(3, 1L);
            ps.setLong(4, totalTokens);
            ps.setLong(5, 0L);
            ps.setString(6, sessionId);
            ps.setString(7, SYSTEM_ADMIN_ID);
            ps.executeUpdate();
        }
    }

    private static void insertUser(String userId, String role, String username) throws Exception {
        try (Connection conn = ai.common.db.TokenLinkDataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement("INSERT INTO users "
                     + "(id, role, username, display_name, password_hash, user_login_status) "
                     + "VALUES (?, ?, ?, 'Initializer', 'not-used', 'logged_in')")) {
            ps.setString(1, userId);
            ps.setString(2, role);
            ps.setString(3, username);
            ps.executeUpdate();
        }
    }

    private static void setUserLoginStatus(String userId, String status) throws Exception {
        try (Connection conn = ai.common.db.TokenLinkDataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE users SET user_login_status = ? WHERE id = ?")) {
            ps.setString(1, status);
            ps.setString(2, userId);
            ps.executeUpdate();
        }
    }

    private static void dropLoginSessionTable() throws Exception {
        try (Connection conn = HikariDS.getConnection("saas");
             Statement statement = conn.createStatement()) {
            statement.executeUpdate("DROP TABLE linkmind_login_session");
        }
    }

    private static void createLoginSessionTable() throws Exception {
        try (Connection conn = HikariDS.getConnection("saas");
             Statement statement = conn.createStatement()) {
            statement.executeUpdate("CREATE TABLE linkmind_login_session ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT, user_id TEXT NOT NULL, "
                    + "session_hash TEXT NOT NULL UNIQUE, expires_at INTEGER NOT NULL, "
                    + "created_at INTEGER NOT NULL, last_seen_at INTEGER)");
            statement.executeUpdate("CREATE INDEX idx_linkmind_login_session_user_id "
                    + "ON linkmind_login_session(user_id)");
        }
    }

    private static String sqliteJdbcUrl(Path path) {
        return "jdbc:sqlite:" + path.toAbsolutePath().toString().replace('\\', '/');
    }

    private static final class ResponseCapture {
        private int status = HttpServletResponse.SC_OK;
        private final StringWriter output = new StringWriter();
        private final PrintWriter writer = new PrintWriter(output);

        private String body() {
            return output.toString();
        }
    }
}

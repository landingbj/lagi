package ai.servlet.api;

import ai.account.CuihuaAccountService;
import ai.account.CuihuaUser;
import ai.dto.ModelApiKey;
import ai.llm.dao.TokenStatisticsDao;
import ai.llm.pojo.TokenStatisticsGuardInfo;
import ai.llm.pojo.TokenStatisticsOverview;
import ai.llm.pojo.TokenStatisticsPageResult;
import ai.llm.pojo.TokenStatisticsRange;
import ai.llm.pojo.TokenStatisticsSessionPageResult;
import ai.llm.pojo.TokenStatisticsSummary;
import ai.servlet.BaseServlet;
import ai.servlet.UserServlet;
import ai.migrate.service.ApiKeyService;
import ai.utils.ApikeyUtil;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Backend-to-backend API used by Cuihua to manage existing Landing API keys and
 * inspect a designated user's model token usage. Every operation requires an
 * active {@code system_admin} account, authenticated by a Landing {@code sk-...} key
 * or a LinkMind management-console session.
 */
public class CuihuaIntegrationApiServlet extends BaseServlet {
    private static final long serialVersionUID = 1L;

    private final ApiKeyService apiKeyService = new ApiKeyService();
    private final CuihuaAccountService accountService = new CuihuaAccountService();
    private final TokenStatisticsDao tokenStatisticsDao = new TokenStatisticsDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        handle(req, resp, HttpMethod.GET);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        handle(req, resp, HttpMethod.POST);
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        handle(req, resp, HttpMethod.PUT);
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        handle(req, resp, HttpMethod.DELETE);
    }

    private void handle(HttpServletRequest req, HttpServletResponse resp, HttpMethod method) throws IOException {
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");
        AdminAuthentication auth = authenticateSystemAdmin(req);
        if (auth.user == null) {
            writeIntegrationError(resp, auth.status, auth.message);
            return;
        }

        RequestTarget target = parseTarget(req);
        if (target == null) {
            writeIntegrationError(resp, HttpServletResponse.SC_NOT_FOUND, "unknown integration resource");
            return;
        }

        try {
            if (target.isLandingApiKeyResource()) {
                handleLandingApiKey(req, resp, method, target);
                return;
            }
            if (target.isTokenUsageResource() && method == HttpMethod.GET) {
                handleTokenUsage(req, resp, target);
                return;
            }
            writeIntegrationError(resp, HttpServletResponse.SC_METHOD_NOT_ALLOWED, "method is not supported for this resource");
        } catch (IllegalArgumentException e) {
            writeIntegrationError(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (SQLException e) {
            writeIntegrationError(resp, HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                    "Cuihua account service is temporarily unavailable");
        } catch (IOException e) {
            writeIntegrationError(resp, HttpServletResponse.SC_BAD_GATEWAY,
                    "Landing API key service is temporarily unavailable");
        }
    }

    private void handleLandingApiKey(HttpServletRequest req, HttpServletResponse resp, HttpMethod method,
                                     RequestTarget target) throws IOException, SQLException {
        if (method == HttpMethod.GET && target.apiKeyId == null && isBlank(target.apiKey)) {
            java.util.List<ModelApiKey> keys = apiKeyService.getLandingApiKeys(target.userId);
            writeSuccess(resp, keys);
            return;
        }
        if (method == HttpMethod.POST && target.apiKeyId == null && isBlank(target.apiKey)) {
            CreateLandingApiKeyRequest body = reqBodyToObj(req, CreateLandingApiKeyRequest.class);
            String name = body == null ? null : body.name();
            ModelApiKey key = apiKeyService.createLandingApiKey(name, target.userId);
            resp.setStatus(HttpServletResponse.SC_CREATED);
            writeSuccess(resp, key);
            return;
        }
        if (method == HttpMethod.DELETE && (target.apiKeyId != null || !isBlank(target.apiKey))) {
            String apiKey = resolveOwnedLandingApiKey(target);
            if (apiKey == null) {
                writeIntegrationError(resp, HttpServletResponse.SC_NOT_FOUND, "Landing API key was not found");
                return;
            }
            apiKeyService.deleteLandingApiKey(apiKey);
            Map<String, Object> data = new HashMap<String, Object>();
            data.put("apiKey", apiKey);
            data.put("deleted", true);
            writeSuccess(resp, data);
            return;
        }
        writeIntegrationError(resp, HttpServletResponse.SC_METHOD_NOT_ALLOWED,
                "Landing API keys support list, create, and delete; create a replacement key instead of updating one");
    }

    private String resolveOwnedLandingApiKey(RequestTarget target) throws IOException {
        java.util.List<ModelApiKey> keys = apiKeyService.getLandingApiKeys(target.userId);
        for (ModelApiKey key : keys) {
            if (key == null || isBlank(key.getApiKey())) {
                continue;
            }
            if (!isBlank(target.apiKey) && target.apiKey.equals(key.getApiKey())) {
                return key.getApiKey();
            }
            if (target.apiKeyId != null && target.apiKeyId.equals(key.getId())) {
                return key.getApiKey();
            }
        }
        return null;
    }

    private void handleTokenUsage(HttpServletRequest req, HttpServletResponse resp, RequestTarget target) throws IOException {
        String rangeParam = req.getParameter("range");
        TokenStatisticsRange range = TokenStatisticsRange.fromQueryParam(rangeParam);
        if ("overview".equals(target.usageView)) {
            TokenStatisticsSummary summary = tokenStatisticsDao.summarize(range, target.userId);
            String echo = rangeParam == null || rangeParam.trim().isEmpty() ? "today" : rangeParam.trim();
            TokenStatisticsOverview overview = TokenStatisticsOverview.builder()
                    .range(echo)
                    .totalTokens(summary.getTotalTokensConsumed())
                    .totalSavedTokens(summary.getTotalSavedTokens())
                    .dailyAvgTokens(summary.getDailyAvgTokensConsumed())
                    .recordCount(summary.getRecordCount())
                    .build();
            writeSuccess(resp, overview);
            return;
        }
        if ("details".equals(target.usageView)) {
            TokenStatisticsPageResult details = tokenStatisticsDao.queryDetails(range,
                    parsePositiveInt(req.getParameter("page"), 1),
                    parsePositiveInt(req.getParameter("pageSize"), 20), target.userId);
            writeSuccess(resp, details);
            return;
        }
        if ("sessions".equals(target.usageView)) {
            TokenStatisticsSessionPageResult sessions = tokenStatisticsDao.querySessions(range,
                    parsePositiveInt(req.getParameter("page"), 1),
                    parsePositiveInt(req.getParameter("pageSize"), 20),
                    parseNullableLong(req.getParameter("startMs")),
                    parseNullableLong(req.getParameter("endMs")), target.userId);
            writeSuccess(resp, sessions);
            return;
        }
        if ("guard".equals(target.usageView)) {
            TokenStatisticsGuardInfo guard = tokenStatisticsDao.guardInfo(target.userId);
            writeSuccess(resp, guard);
            return;
        }
        writeIntegrationError(resp, HttpServletResponse.SC_NOT_FOUND, "unknown token usage view");
    }

    private AdminAuthentication authenticateSystemAdmin(HttpServletRequest req) {
        String apiKey = ApikeyUtil.extractBearerToken(req.getHeader("Authorization"));
        if (isBlank(apiKey)) {
            apiKey = optionalText(req.getHeader("x-api-key"));
        }
        if (!isBlank(apiKey)) {
            if (!ApikeyUtil.validateModelApiKey(apiKey)) {
                return AdminAuthentication.unauthorized("system administrator API key is invalid");
            }
            try {
                CuihuaUser user = accountService.findActiveUserById(apiKeyService.resolveLandingUserId(apiKey));
                if (user == null) {
                    return AdminAuthentication.unauthorized("system administrator account is unavailable");
                }
                return accountService.isSystemAdmin(user)
                        ? AdminAuthentication.authorized(user)
                        : AdminAuthentication.forbidden("system_admin role is required");
            } catch (SQLException | IOException e) {
                return AdminAuthentication.unavailable("system administrator authentication is unavailable");
            }
        }
        try {
            CuihuaUser user = accountService.resolveSession(readSessionCookie(req));
            if (user == null) {
                return AdminAuthentication.unauthorized("system administrator authentication is required");
            }
            return accountService.isSystemAdmin(user)
                    ? AdminAuthentication.authorized(user)
                    : AdminAuthentication.forbidden("system_admin role is required");
        } catch (SQLException e) {
            return AdminAuthentication.unavailable("system administrator authentication is unavailable");
        }
    }

    private RequestTarget parseTarget(HttpServletRequest req) {
        String path = req.getRequestURI();
        String contextPath = req.getContextPath();
        if (contextPath != null && !contextPath.isEmpty() && path.startsWith(contextPath)) {
            path = path.substring(contextPath.length());
        }
        final String prefix = "/v1/integration/users/";
        if (!path.startsWith(prefix)) {
            return null;
        }
        String[] segments = path.substring(prefix.length()).split("/", -1);
        if (segments.length < 2 || isBlank(segments[0])) {
            return null;
        }
        String userId = decodePathSegment(segments[0]);
        if (userId == null) {
            return null;
        }
        if ("client-api-keys".equals(segments[1])) {
            if (segments.length == 2) {
                String apiKey = optionalText(req.getParameter("apiKey"));
                return RequestTarget.landingApiKeys(userId, null, apiKey);
            }
            if (segments.length == 3) {
                try {
                    long id = Long.parseLong(segments[2]);
                    return id > 0 ? RequestTarget.landingApiKeys(userId, id, null) : null;
                } catch (NumberFormatException e) {
                    return null;
                }
            }
            return null;
        }
        if ("token-usage".equals(segments[1]) && segments.length == 3) {
            return RequestTarget.tokenUsage(userId, segments[2]);
        }
        return null;
    }

    private static String readSessionCookie(HttpServletRequest req) {
        Cookie[] cookies = req.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (UserServlet.SESSION_COOKIE_NAME.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    private static String decodePathSegment(String segment) {
        try {
            return URLDecoder.decode(segment, "UTF-8");
        } catch (Exception e) {
            return null;
        }
    }

    private static int parsePositiveInt(String raw, int defaultValue) {
        if (isBlank(raw)) {
            return defaultValue;
        }
        int value = Integer.parseInt(raw.trim());
        if (value < 1) {
            throw new IllegalArgumentException("must be >= 1");
        }
        return value;
    }

    private static Long parseNullableLong(String raw) {
        return isBlank(raw) ? null : Long.parseLong(raw.trim());
    }

    private void writeSuccess(HttpServletResponse resp, Object data) throws IOException {
        Map<String, Object> body = new HashMap<String, Object>();
        body.put("status", "success");
        body.put("data", data);
        writeJson(resp, gson.toJson(body));
    }

    private void writeIntegrationError(HttpServletResponse resp, int status, String message) throws IOException {
        resp.setStatus(status);
        Map<String, String> error = new HashMap<String, String>();
        error.put("status", "failed");
        error.put("msg", message);
        writeJson(resp, gson.toJson(error));
    }

    private static void writeJson(HttpServletResponse resp, String json) throws IOException {
        PrintWriter writer = resp.getWriter();
        writer.print(json);
        writer.flush();
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static String optionalText(String value) {
        return isBlank(value) ? null : value.trim();
    }

    private enum HttpMethod {
        GET, POST, PUT, DELETE
    }

    private static final class RequestTarget {
        private final String userId;
        private final Long apiKeyId;
        private final String apiKey;
        private final String usageView;

        private RequestTarget(String userId, Long apiKeyId, String apiKey, String usageView) {
            this.userId = userId;
            this.apiKeyId = apiKeyId;
            this.apiKey = apiKey;
            this.usageView = usageView;
        }

        private static RequestTarget landingApiKeys(String userId, Long apiKeyId, String apiKey) {
            return new RequestTarget(userId, apiKeyId, apiKey, null);
        }

        private static RequestTarget tokenUsage(String userId, String usageView) {
            return new RequestTarget(userId, null, null, usageView);
        }

        private boolean isLandingApiKeyResource() {
            return usageView == null;
        }

        private boolean isTokenUsageResource() {
            return usageView != null;
        }
    }

    private static final class AdminAuthentication {
        private final CuihuaUser user;
        private final int status;
        private final String message;

        private AdminAuthentication(CuihuaUser user, int status, String message) {
            this.user = user;
            this.status = status;
            this.message = message;
        }

        private static AdminAuthentication authorized(CuihuaUser user) {
            return new AdminAuthentication(user, HttpServletResponse.SC_OK, null);
        }

        private static AdminAuthentication unauthorized(String message) {
            return new AdminAuthentication(null, HttpServletResponse.SC_UNAUTHORIZED, message);
        }

        private static AdminAuthentication forbidden(String message) {
            return new AdminAuthentication(null, HttpServletResponse.SC_FORBIDDEN, message);
        }

        private static AdminAuthentication unavailable(String message) {
            return new AdminAuthentication(null, HttpServletResponse.SC_SERVICE_UNAVAILABLE, message);
        }
    }

    private static final class CreateLandingApiKeyRequest {
        private String remark;
        private String name;

        private String name() {
            return !isBlank(remark) ? remark : name;
        }
    }
}

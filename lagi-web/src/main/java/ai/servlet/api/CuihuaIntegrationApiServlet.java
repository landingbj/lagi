package ai.servlet.api;

import ai.account.LocalUserApiKey;
import ai.account.LocalUserApiKeyService;
import ai.account.LocalUserApiKeyService.UserKeyException;
import ai.llm.dao.TokenStatisticsDao;
import ai.llm.pojo.TokenStatisticsPageResult;
import ai.llm.pojo.TokenStatisticsRange;
import ai.llm.pojo.TokenStatisticsSessionPageResult;
import ai.llm.pojo.TokenStatisticsSummary;
import ai.servlet.BaseServlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Box-internal integration API used by Cuihua. It manages only LinkMind-owned
 * {@code lmk_} keys and never requests a remote SaaS/Landing {@code sk-} key.
 * Network placement is the security boundary for these administrative routes.
 */
public class CuihuaIntegrationApiServlet extends BaseServlet {
    private static final long serialVersionUID = 1L;
    private static final int MAX_PAGE_SIZE = 100;

    private final LocalUserApiKeyService localKeyService = new LocalUserApiKeyService();
    private final TokenStatisticsDao tokenStatisticsDao = new TokenStatisticsDao();

    @Override
    public void init() throws ServletException {
        try {
            localKeyService.initialize();
        } catch (SQLException e) {
            throw new ServletException("local User Key storage initialization failed", e);
        }
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if ("PATCH".equalsIgnoreCase(req.getMethod())) {
            handle(req, resp, HttpMethod.PATCH);
            return;
        }
        super.service(req, resp);
    }

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
        handle(req, resp, HttpMethod.PATCH);
    }

    private void handle(HttpServletRequest req, HttpServletResponse resp, HttpMethod method) throws IOException {
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");
        if (!isBoxInternalRequest(req)) {
            writeError(resp, HttpServletResponse.SC_FORBIDDEN, "INTERNAL_NETWORK_REQUIRED",
                    "this integration API is available only from the LinkMind box internal network");
            return;
        }

        RequestTarget target = parseTarget(req);
        if (target == null) {
            writeError(resp, HttpServletResponse.SC_NOT_FOUND, "RESOURCE_NOT_FOUND", "unknown integration resource");
            return;
        }
        if (target.legacyClientApiKeys) {
            writeError(resp, HttpServletResponse.SC_GONE, "LEGACY_CLIENT_API_KEY_ENDPOINT_REMOVED",
                    "Use /v1/integration/users/{userId}/user-api-keys for local lmk_ keys.");
            return;
        }

        try {
            if (target.userApiKeys) {
                handleUserApiKeys(req, resp, method, target);
                return;
            }
            if (target.usageView != null && method == HttpMethod.GET) {
                handleTokenUsage(req, resp, target);
                return;
            }
            writeError(resp, HttpServletResponse.SC_METHOD_NOT_ALLOWED, "METHOD_NOT_ALLOWED",
                    "method is not supported for this resource");
        } catch (UserKeyException e) {
            writeUserKeyError(resp, e);
        } catch (IllegalArgumentException e) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "INVALID_REQUEST", e.getMessage());
        } catch (SQLException e) {
            writeError(resp, HttpServletResponse.SC_SERVICE_UNAVAILABLE, "LOCAL_DATABASE_UNAVAILABLE",
                    "local LinkMind storage is temporarily unavailable");
        } catch (Exception e) {
            writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "INTEGRATION_REQUEST_FAILED",
                    "LinkMind could not process the integration request");
        }
    }

    private void handleUserApiKeys(HttpServletRequest req, HttpServletResponse resp, HttpMethod method,
                                   RequestTarget target) throws Exception {
        if (method == HttpMethod.GET && target.keyId == null && !target.revoke) {
            String status = optionalText(req.getParameter("status"));
            int page = parsePage(req.getParameter("page"), 1);
            int pageSize = parsePageSize(req.getParameter("pageSize"));
            List<LocalUserApiKey> keys = localKeyService.list(target.userId, status, page, pageSize);
            Map<String, Object> data = new LinkedHashMap<String, Object>();
            data.put("page", page);
            data.put("pageSize", pageSize);
            data.put("total", localKeyService.count(target.userId, status));
            data.put("items", metadataList(keys));
            writeSuccess(resp, data);
            return;
        }
        if (method == HttpMethod.POST && target.keyId == null && !target.revoke) {
            UserKeyBody body = reqBodyToObj(req, UserKeyBody.class);
            LocalUserApiKey key = localKeyService.create(target.userId, body == null ? null : body.remark);
            resp.setStatus(HttpServletResponse.SC_CREATED);
            writeSuccess(resp, createResponse(key));
            return;
        }
        if (method == HttpMethod.PATCH && target.keyId != null && !target.revoke) {
            UserKeyBody body = reqBodyToObj(req, UserKeyBody.class);
            LocalUserApiKey key = localKeyService.updateRemark(target.userId, target.keyId,
                    body == null ? null : body.remark);
            writeSuccess(resp, metadata(key));
            return;
        }
        if (method == HttpMethod.POST && target.keyId != null && target.revoke) {
            LocalUserApiKey key = localKeyService.revoke(target.userId, target.keyId);
            writeSuccess(resp, metadata(key));
            return;
        }
        writeError(resp, HttpServletResponse.SC_METHOD_NOT_ALLOWED, "METHOD_NOT_ALLOWED",
                "local User Key supports list, create, update remark, and revoke");
    }

    private void handleTokenUsage(HttpServletRequest req, HttpServletResponse resp, RequestTarget target)
            throws SQLException, UserKeyException, IOException {
        // Resolving the key-list count deliberately verifies that the shared Cuihua user exists.
        localKeyService.count(target.userId, "all");
        String rangeParam = optionalText(req.getParameter("range"));
        String rangeEcho = rangeParam == null ? "month" : rangeParam;
        TokenStatisticsRange range = TokenStatisticsRange.fromQueryParam(rangeEcho);
        if ("overview".equals(target.usageView)) {
            TokenStatisticsSummary summary = tokenStatisticsDao.summarize(range, target.userId);
            writeSuccess(resp, overview(rangeEcho, summary));
            return;
        }
        int page = parsePage(req.getParameter("page"), 1);
        int pageSize = parsePageSize(req.getParameter("pageSize"));
        if ("details".equals(target.usageView)) {
            TokenStatisticsPageResult result = tokenStatisticsDao.queryDetails(range, page, pageSize, target.userId);
            Map<String, Object> data = new LinkedHashMap<String, Object>();
            data.put("page", result.getPage());
            data.put("pageSize", result.getPageSize());
            data.put("total", result.getTotal());
            data.put("items", result.getRecords());
            writeSuccess(resp, data);
            return;
        }
        if ("sessions".equals(target.usageView)) {
            TokenStatisticsSessionPageResult result = tokenStatisticsDao.querySessions(range, page, pageSize,
                    parseNullableLong(req.getParameter("startMs")), parseNullableLong(req.getParameter("endMs")),
                    target.userId);
            Map<String, Object> data = new LinkedHashMap<String, Object>();
            data.put("page", result.getPage());
            data.put("pageSize", result.getPageSize());
            data.put("total", result.getTotal());
            data.put("items", result.getRecords());
            writeSuccess(resp, data);
            return;
        }
        writeError(resp, HttpServletResponse.SC_NOT_FOUND, "RESOURCE_NOT_FOUND", "unknown token usage view");
    }

    private Map<String, Object> overview(String range, TokenStatisticsSummary summary) {
        Map<String, Object> data = new LinkedHashMap<String, Object>();
        data.put("range", range);
        data.put("totalPromptTokens", summary.getTotalPromptTokens());
        data.put("totalCompletionTokens", summary.getTotalCompletionTokens());
        data.put("totalTokens", summary.getTotalTokensConsumed());
        data.put("totalCost", summary.getTotalCost());
        data.put("currency", summary.getCurrency());
        data.put("pricedRecordCount", summary.getPricedRecordCount());
        data.put("unpricedRecordCount", summary.getUnpricedRecordCount());
        data.put("recordCount", summary.getRecordCount());
        data.put("costsByCurrency", summary.getCostsByCurrency());
        return data;
    }

    private static List<Map<String, Object>> metadataList(List<LocalUserApiKey> keys) {
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        for (LocalUserApiKey key : keys) {
            result.add(metadata(key));
        }
        return result;
    }

    private static Map<String, Object> createResponse(LocalUserApiKey key) {
        Map<String, Object> result = metadata(key);
        result.put("apiKey", key.getApiKey());
        return result;
    }

    private static Map<String, Object> metadata(LocalUserApiKey key) {
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("id", key.getId());
        result.put("userId", key.getUserId());
        result.put("keyHint", key.getKeyHint());
        result.put("remark", key.getRemark());
        result.put("status", key.getStatus());
        result.put("createdAt", key.getCreatedAt());
        result.put("lastUsedAt", key.getLastUsedAt());
        result.put("revokedAt", key.getRevokedAt());
        return result;
    }

    private RequestTarget parseTarget(HttpServletRequest req) {
        String path = req.getRequestURI();
        String contextPath = req.getContextPath();
        if (contextPath != null && !contextPath.isEmpty() && path.startsWith(contextPath)) {
            path = path.substring(contextPath.length());
        }
        String prefix = "/v1/integration/users/";
        if (!path.startsWith(prefix)) {
            return null;
        }
        String[] segments = path.substring(prefix.length()).split("/", -1);
        if (segments.length < 2 || isBlank(segments[0])) {
            return null;
        }
        String userId = decodePathSegment(segments[0]);
        if (isBlank(userId)) {
            return null;
        }
        if ("client-api-keys".equals(segments[1])) {
            return RequestTarget.legacy(userId);
        }
        if ("token-usage".equals(segments[1]) && segments.length == 3) {
            return RequestTarget.usage(userId, segments[2]);
        }
        if (!"user-api-keys".equals(segments[1])) {
            return null;
        }
        if (segments.length == 2) {
            return RequestTarget.keys(userId, null, false);
        }
        if (segments.length == 3 || segments.length == 4 && "revoke".equals(segments[3])) {
            Long keyId = parseKeyId(segments[2]);
            return keyId == null ? null : RequestTarget.keys(userId, keyId, segments.length == 4);
        }
        return null;
    }

    private static Long parseKeyId(String value) {
        try {
            long id = Long.parseLong(value);
            return id > 0 ? id : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static boolean isBoxInternalRequest(HttpServletRequest req) {
        String address = req.getRemoteAddr();
        if (address == null) {
            return false;
        }
        if ("127.0.0.1".equals(address) || "::1".equals(address) || "0:0:0:0:0:0:0:1".equals(address)) {
            return true;
        }
        // Default Docker bridge and user-defined Docker network address space.
        String[] parts = address.split("\\.");
        if (parts.length == 4 && "172".equals(parts[0])) {
            try {
                int second = Integer.parseInt(parts[1]);
                return second >= 16 && second <= 31;
            } catch (NumberFormatException ignored) {
                return false;
            }
        }
        return false;
    }

    private static String decodePathSegment(String segment) {
        try {
            return URLDecoder.decode(segment, "UTF-8");
        } catch (Exception e) {
            return null;
        }
    }

    private static int parsePage(String raw, int defaultValue) {
        if (isBlank(raw)) {
            return defaultValue;
        }
        int value = Integer.parseInt(raw.trim());
        if (value < 1) {
            throw new IllegalArgumentException("INVALID_PAGINATION: page must be >= 1");
        }
        return value;
    }

    private static int parsePageSize(String raw) {
        if (isBlank(raw)) {
            return 20;
        }
        int value = Integer.parseInt(raw.trim());
        if (value < 1 || value > MAX_PAGE_SIZE) {
            throw new IllegalArgumentException("INVALID_PAGINATION: pageSize must be between 1 and " + MAX_PAGE_SIZE);
        }
        return value;
    }

    private static Long parseNullableLong(String raw) {
        return isBlank(raw) ? null : Long.parseLong(raw.trim());
    }

    private void writeUserKeyError(HttpServletResponse resp, UserKeyException e) throws IOException {
        switch (e.getReason()) {
            case USER_NOT_FOUND:
                writeError(resp, HttpServletResponse.SC_NOT_FOUND, "USER_NOT_FOUND", "user does not exist");
                return;
            case USER_KEY_NOT_FOUND:
                writeError(resp, HttpServletResponse.SC_NOT_FOUND, "USER_KEY_NOT_FOUND", "local User Key was not found");
                return;
            case USER_KEY_ALREADY_REVOKED:
                writeError(resp, HttpServletResponse.SC_CONFLICT, "USER_KEY_ALREADY_REVOKED", "local User Key is already revoked");
                return;
            case INVALID_REMARK:
                writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "INVALID_REMARK", "remark must not exceed 120 characters");
                return;
            case INVALID_STATUS:
                writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "INVALID_STATUS", "status must be active, revoked, or all");
                return;
            default:
                writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "INVALID_REQUEST", "invalid local User Key request");
        }
    }

    private void writeSuccess(HttpServletResponse resp, Object data) throws IOException {
        Map<String, Object> body = new LinkedHashMap<String, Object>();
        body.put("status", "success");
        body.put("data", data);
        writeJson(resp, gson.toJson(body));
    }

    private void writeError(HttpServletResponse resp, int status, String code, String message) throws IOException {
        resp.setStatus(status);
        Map<String, String> error = new LinkedHashMap<String, String>();
        error.put("status", "failed");
        error.put("code", code);
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

    private enum HttpMethod { GET, POST, PATCH }

    private static final class UserKeyBody {
        private String remark;
    }

    private static final class RequestTarget {
        private final String userId;
        private final Long keyId;
        private final boolean revoke;
        private final boolean userApiKeys;
        private final boolean legacyClientApiKeys;
        private final String usageView;

        private RequestTarget(String userId, Long keyId, boolean revoke, boolean userApiKeys,
                              boolean legacyClientApiKeys, String usageView) {
            this.userId = userId;
            this.keyId = keyId;
            this.revoke = revoke;
            this.userApiKeys = userApiKeys;
            this.legacyClientApiKeys = legacyClientApiKeys;
            this.usageView = usageView;
        }

        private static RequestTarget keys(String userId, Long keyId, boolean revoke) {
            return new RequestTarget(userId, keyId, revoke, true, false, null);
        }

        private static RequestTarget legacy(String userId) {
            return new RequestTarget(userId, null, false, false, true, null);
        }

        private static RequestTarget usage(String userId, String usageView) {
            return new RequestTarget(userId, null, false, false, false, usageView);
        }
    }
}

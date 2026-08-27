package ai.servlet.api;

import ai.account.CuihuaAccountService;
import ai.account.CuihuaUser;
import ai.account.LocalUserApiKey;
import ai.account.LocalUserApiKeyService;
import ai.llm.dao.TokenStatisticsDao;
import ai.llm.pojo.TokenStatisticsDetail;
import ai.llm.pojo.TokenStatisticsGuardInfo;
import ai.llm.pojo.TokenStatisticsOverview;
import ai.llm.pojo.TokenStatisticsPageResult;
import ai.llm.pojo.TokenStatisticsRange;
import ai.llm.pojo.TokenStatisticsSummary;
import ai.servlet.BaseServlet;
import ai.servlet.UserServlet;
import ai.utils.ApikeyUtil;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Per-user token usage API. The account is taken from an authenticated
 * local {@code lmk_} key or LinkMind browser session; it is never accepted as
 * a query parameter.
 */
@Slf4j
public class TokenStatisticsApiServlet extends BaseServlet {
    private static final long serialVersionUID = 1L;

    private final TokenStatisticsDao tokenStatisticsDao = new TokenStatisticsDao();
    private final CuihuaAccountService accountService = new CuihuaAccountService();
    private final LocalUserApiKeyService localUserApiKeyService = new LocalUserApiKeyService();
    private final TokenUsageAuthorizationService authorizationService = new TokenUsageAuthorizationService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");
        CuihuaUser actor;
        TokenUsageAuthorizationService.TokenUsageScope usageScope;
        try {
            actor = resolveAuthenticatedUser(req);
            usageScope = authorizationService.authorizeAuthenticatedUser(actor, req.getParameter("targetUserId"));
        } catch (SQLException e) {
            resp.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            writeErrorJson(resp, "LOCAL_STORAGE_UNAVAILABLE",
                    "account or local User Key storage is temporarily unavailable");
            return;
        } catch (TokenUsageAuthorizationService.TokenUsageAuthorizationException e) {
            writeAuthorizationError(resp, e);
            return;
        }

        String uri = req.getRequestURI();
        try {
            if (uri.endsWith("/overview")) {
                writeOverview(req, resp, usageScope);
            } else if (uri.endsWith("/details")) {
                writeDetails(req, resp, usageScope);
            } else if (uri.endsWith("/guard")) {
                writeGuard(resp, usageScope.getQueryUserId());
            } else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                writeJson(resp, "{\"error\":\"not found\"}");
            }
        } catch (NumberFormatException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            writeErrorJson(resp, "INVALID_REQUEST", "invalid number: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            writeErrorJson(resp, "INVALID_REQUEST", e.getMessage());
        }
    }

    private void writeOverview(HttpServletRequest req, HttpServletResponse resp,
                               TokenUsageAuthorizationService.TokenUsageScope usageScope) throws IOException {
        String rangeParam = req.getParameter("range");
        TokenStatisticsRange range = TokenStatisticsRange.fromQueryParam(rangeParam);
        TokenStatisticsSummary summary = tokenStatisticsDao.summarize(range, usageScope.getQueryUserId());
        String echo = rangeParam == null || rangeParam.isEmpty() ? "today" : rangeParam.trim();
        TokenStatisticsOverview overview = TokenStatisticsOverview.builder()
                .range(echo)
                .totalTokens(summary.getTotalTokensConsumed())
                .totalSavedTokens(summary.getTotalSavedTokens())
                .dailyAvgTokens(summary.getDailyAvgTokensConsumed())
                .recordCount(summary.getRecordCount())
                .totalPromptTokens(summary.getTotalPromptTokens())
                .totalCompletionTokens(summary.getTotalCompletionTokens())
                .totalCost(summary.getTotalCost())
                .currency(summary.getCurrency())
                .pricedRecordCount(summary.getPricedRecordCount())
                .unpricedRecordCount(summary.getUnpricedRecordCount())
                .costsByCurrency(summary.getCostsByCurrency())
                .scope(usageScope.getScope().getValue())
                .targetUserId(usageScope.getTargetUserId())
                .build();
        writeJson(resp, gson.toJson(overview));
    }

    private void writeGuard(HttpServletResponse resp, String userId) throws IOException {
        TokenStatisticsGuardInfo info = tokenStatisticsDao.guardInfo(userId);
        writeJson(resp, gson.toJson(info));
    }

    private void writeDetails(HttpServletRequest req, HttpServletResponse resp,
                              TokenUsageAuthorizationService.TokenUsageScope usageScope) throws IOException {
        TokenStatisticsRange range = TokenStatisticsRange.fromQueryParam(req.getParameter("range"));
        int page = parsePositiveInt(req.getParameter("page"), 1);
        int pageSize = parsePositiveInt(req.getParameter("pageSize"), 20);
        TokenStatisticsPageResult result = tokenStatisticsDao.queryDetails(
                range, page, pageSize, usageScope.getQueryUserId());
        populateUserDisplayNames(result.getRecords());
        result.setScope(usageScope.getScope().getValue());
        result.setTargetUserId(usageScope.getTargetUserId());
        writeJson(resp, gson.toJson(result));
    }

    private void populateUserDisplayNames(List<TokenStatisticsDetail> records) {
        if (records == null || records.isEmpty()) {
            return;
        }
        Set<String> userIds = new LinkedHashSet<String>();
        for (TokenStatisticsDetail record : records) {
            if (record != null && optionalText(record.getUserId()) != null) {
                userIds.add(record.getUserId().trim());
            }
        }

        Map<String, String> displayNames = Collections.emptyMap();
        try {
            displayNames = accountService.findUserDisplayNames(userIds);
        } catch (SQLException e) {
            log.warn("read TokenLink user display names failed; falling back to user IDs", e);
        }
        applyUserDisplayNames(records, displayNames);
    }

    static void applyUserDisplayNames(List<TokenStatisticsDetail> records, Map<String, String> displayNames) {
        if (records == null) {
            return;
        }
        for (TokenStatisticsDetail record : records) {
            if (record == null) {
                continue;
            }
            String displayName = displayNames == null ? null : displayNames.get(record.getUserId());
            String resolvedName = optionalText(displayName);
            record.setUserDisplayName(resolvedName == null ? optionalText(record.getUserId()) : resolvedName);
        }
    }

    private CuihuaUser resolveAuthenticatedUser(HttpServletRequest req) throws SQLException {
        String key = ApikeyUtil.extractBearerToken(req.getHeader("Authorization"));
        if (key == null || key.isEmpty()) {
            key = optionalText(req.getHeader("x-api-key"));
        }
        if (key != null) {
            LocalUserApiKey localKey = localUserApiKeyService.authenticate(key);
            return localKey == null ? null : accountService.findActiveUserById(localKey.getUserId());
        }
        return accountService.resolveSession(readSessionCookie(req));
    }

    private String readSessionCookie(HttpServletRequest req) {
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

    private static String optionalText(String value) {
        return value == null || value.trim().isEmpty() ? null : value.trim();
    }

    private static int parsePositiveInt(String raw, int defaultValue) {
        if (raw == null || raw.isEmpty()) {
            return defaultValue;
        }
        int value = Integer.parseInt(raw.trim());
        if (value < 1) {
            throw new IllegalArgumentException("must be >= 1");
        }
        return value;
    }

    private static void writeJson(HttpServletResponse resp, String json) throws IOException {
        PrintWriter out = resp.getWriter();
        out.print(json);
        out.flush();
    }

    private void writeAuthorizationError(HttpServletResponse resp,
                                         TokenUsageAuthorizationService.TokenUsageAuthorizationException e)
            throws IOException {
        switch (e.getReason()) {
            case AUTHENTICATION_REQUIRED:
                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                writeErrorJson(resp, "AUTHENTICATION_REQUIRED", "authentication required");
                return;
            case TARGET_USER_NOT_FOUND:
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                writeErrorJson(resp, "TOKEN_USAGE_TARGET_USER_NOT_FOUND", "target user does not exist");
                return;
            case ACCESS_DENIED:
            case ACTOR_ROLE_NOT_ALLOWED:
                resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                writeErrorJson(resp, "TOKEN_USAGE_ACCESS_DENIED", "token usage access is denied");
                return;
            default:
                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                writeErrorJson(resp, "AUTHENTICATION_REQUIRED", "authentication required");
        }
    }

    private void writeErrorJson(HttpServletResponse resp, String code, String message) throws IOException {
        Map<String, String> error = new HashMap<String, String>();
        error.put("code", code);
        error.put("error", message);
        writeJson(resp, gson.toJson(error));
    }
}

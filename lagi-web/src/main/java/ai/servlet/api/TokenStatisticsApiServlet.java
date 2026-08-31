package ai.servlet.api;

import ai.account.CuihuaAccountService;
import ai.account.CuihuaUser;
import ai.account.CuihuaUserIdentity;
import ai.account.LocalUserApiKey;
import ai.account.LocalUserApiKeyService;
import ai.common.pojo.Backend;
import ai.config.ContextLoader;
import ai.llm.dao.TokenStatisticsDao;
import ai.llm.pojo.TokenStatisticsDetail;
import ai.llm.pojo.TokenStatisticsGuardInfo;
import ai.llm.pojo.TokenStatisticsOverview;
import ai.llm.pojo.TokenStatisticsPageResult;
import ai.llm.pojo.TokenStatisticsRange;
import ai.llm.pojo.TokenStatisticsSummary;
import ai.llm.pojo.TokenStatisticsTrendBucket;
import ai.llm.pojo.TokenStatisticsUserTrend;
import ai.llm.pojo.TokenStatisticsUserModelUsage;
import ai.llm.pojo.TokenStatisticsUserUsage;
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
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Per-user token usage API. The account is taken from an authenticated
 * local {@code lmk_} key or LinkMind browser session; it is never accepted as
 * a query parameter.
 */
@Slf4j
public class TokenStatisticsApiServlet extends BaseServlet {
    private static final long serialVersionUID = 1L;
    private static final String UNATTRIBUTED_KEY = "__unattributed__";
    private static final String OTHER_PLATFORM = "__other__";

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
            if (uri.endsWith("/dashboard")) {
                writeDashboard(req, resp, usageScope);
            } else if (uri.endsWith("/overview")) {
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

    private void writeDashboard(HttpServletRequest req, HttpServletResponse resp,
                                TokenUsageAuthorizationService.TokenUsageScope usageScope) throws IOException {
        boolean usageView = "usage".equalsIgnoreCase(optionalText(req.getParameter("view")));
        DashboardDateRange dateRange = parseDashboardDateRange(
                req.getParameter("range"), req.getParameter("startDate"), req.getParameter("endDate"),
                ZoneId.systemDefault());
        DashboardDateRange previousRange = usageView ? null
                : previousDashboardDateRange(dateRange, ZoneId.systemDefault());
        int requestedPage = parsePositiveInt(req.getParameter("page"), 1);
        int pageSize = Math.min(100, parsePositiveInt(req.getParameter("pageSize"), 20));
        String search = optionalText(req.getParameter("search"));
        String sort = normalizeDashboardSort(req.getParameter("sort"), usageView ? "tokens" : "cost");
        String direction = "asc".equalsIgnoreCase(req.getParameter("direction")) ? "asc" : "desc";
        List<String> configuredPlatforms = configuredPlatformTypes();
        String platform = resolvePlatform(req.getParameter("platform"), configuredPlatforms);

        List<TokenStatisticsUserUsage> rawUsers = tokenStatisticsDao.queryUserUsage(
                dateRange.getStartMs(), dateRange.getEndExclusiveMs(), usageScope.getQueryUserId(),
                platform, configuredPlatforms);
        List<TokenStatisticsUserUsage> rawPreviousUsers = previousRange == null
                ? Collections.<TokenStatisticsUserUsage>emptyList()
                : tokenStatisticsDao.queryUserUsage(previousRange.getStartMs(), previousRange.getEndExclusiveMs(),
                usageScope.getQueryUserId(), platform, configuredPlatforms);
        Set<String> userIds = collectUsageUserIds(rawUsers);
        userIds.addAll(collectUsageUserIds(rawPreviousUsers));
        Map<String, CuihuaUserIdentity> identities;
        try {
            identities = accountService.findUserIdentities(userIds);
        } catch (SQLException e) {
            log.warn("read TokenLink user identities for usage dashboard failed", e);
            resp.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            writeErrorJson(resp, "LOCAL_STORAGE_UNAVAILABLE", "user identity storage is temporarily unavailable");
            return;
        }

        List<TokenStatisticsUserUsage> users = consolidateUserUsage(rawUsers, identities);
        List<TokenStatisticsUserUsage> previousUsers = consolidateUserUsage(rawPreviousUsers, identities);
        Map<String, Set<String>> modelsByUser = Collections.emptyMap();
        if (usageView) {
            List<TokenStatisticsUserModelUsage> rawModels = tokenStatisticsDao.queryUserModels(
                    dateRange.getStartMs(), dateRange.getEndExclusiveMs(), usageScope.getQueryUserId(),
                    platform, configuredPlatforms);
            modelsByUser = consolidateUserModels(rawModels, identities);
            applyModelCounts(users, modelsByUser);
        }
        users = filterUserUsage(users, search);
        previousUsers = filterUserUsage(previousUsers, search);

        Set<String> selectedUserKeys = collectCanonicalUserKeys(users);
        List<TokenStatisticsUserTrend> rawTrend = tokenStatisticsDao.queryUserTrend(
                dateRange.getStartMs(), dateRange.getEndExclusiveMs(), usageScope.getQueryUserId(),
                dateRange.getBucket(), platform, configuredPlatforms);
        List<TokenStatisticsUserTrend> trend = consolidateTrend(rawTrend, identities, selectedUserKeys);

        Map<String, Object> summary = summarizeUsers(users);
        summary.put("modelCount", countDistinctModels(modelsByUser, selectedUserKeys));
        summary.put("averageTokensPerRequest", averageTokens(
                ((Number) summary.get("totalTokens")).longValue(),
                ((Number) summary.get("recordCount")).longValue()));

        List<TokenStatisticsUserUsage> tableUsers = usageView ? users : pricedUsers(users);
        sortUserUsage(tableUsers, sort, direction);
        int total = tableUsers.size();
        int totalPages = Math.max(1, (int) Math.ceil((double) total / (double) pageSize));
        int page = Math.min(requestedPage, totalPages);
        int fromIndex = total == 0 ? 0 : (page - 1) * pageSize;
        int toIndex = Math.min(total, fromIndex + pageSize);
        List<TokenStatisticsUserUsage> pageRows = new ArrayList<TokenStatisticsUserUsage>(
                tableUsers.subList(fromIndex, toIndex));

        Map<String, Object> userPage = new LinkedHashMap<String, Object>();
        userPage.put("page", page);
        userPage.put("pageSize", pageSize);
        userPage.put("total", total);
        userPage.put("sort", sort);
        userPage.put("direction", direction);
        userPage.put("records", pageRows);

        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("view", usageView ? "usage" : "cost");
        result.put("range", dateRange.isAllTime() ? "all" : "custom");
        result.put("startDate", dateRange.getStartDate() == null ? null : dateRange.getStartDate().toString());
        result.put("endDate", dateRange.getEndDate() == null ? null : dateRange.getEndDate().toString());
        result.put("bucket", dateRange.getBucket().getValue());
        result.put("platform", platform);
        result.put("platforms", platformOptions(configuredPlatforms));
        result.put("summary", summary);
        if (!usageView) {
            Map<String, Object> previousSummary = previousRange == null ? null : summarizeUsers(previousUsers);
            result.put("previousSummary", previousSummary);
            result.put("comparison", costComparison(summary, previousSummary));
        }
        result.put("trend", trend);
        result.put("users", userPage);
        result.put("scope", usageScope.getScope().getValue());
        result.put("targetUserId", usageScope.getTargetUserId());
        writeJson(resp, gson.toJson(result));
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

    static DashboardDateRange parseDashboardDateRange(String range, String startDate, String endDate, ZoneId zone) {
        if ("all".equalsIgnoreCase(optionalText(range))) {
            return new DashboardDateRange(null, null, 0L, Long.MAX_VALUE,
                    TokenStatisticsTrendBucket.MONTH, true);
        }
        ZoneId effectiveZone = zone == null ? ZoneId.systemDefault() : zone;
        LocalDate start;
        LocalDate end;
        try {
            if (optionalText(startDate) == null && optionalText(endDate) == null) {
                start = LocalDate.now(effectiveZone);
                end = start;
            } else {
                if (optionalText(startDate) == null || optionalText(endDate) == null) {
                    throw new IllegalArgumentException("startDate and endDate must be provided together");
                }
                start = LocalDate.parse(startDate.trim());
                end = LocalDate.parse(endDate.trim());
            }
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("dates must use yyyy-MM-dd");
        }
        if (start.isAfter(end)) {
            throw new IllegalArgumentException("startDate must not be after endDate");
        }
        long inclusiveDays = ChronoUnit.DAYS.between(start, end) + 1L;
        TokenStatisticsTrendBucket bucket = inclusiveDays == 1L
                ? TokenStatisticsTrendBucket.HOUR
                : (inclusiveDays <= 90L ? TokenStatisticsTrendBucket.DAY : TokenStatisticsTrendBucket.MONTH);
        return new DashboardDateRange(start, end,
                start.atStartOfDay(effectiveZone).toInstant().toEpochMilli(),
                end.plusDays(1L).atStartOfDay(effectiveZone).toInstant().toEpochMilli(), bucket, false);
    }

    static DashboardDateRange previousDashboardDateRange(DashboardDateRange current, ZoneId zone) {
        if (current == null || current.isAllTime()) {
            return null;
        }
        ZoneId effectiveZone = zone == null ? ZoneId.systemDefault() : zone;
        long days = ChronoUnit.DAYS.between(current.getStartDate(), current.getEndDate()) + 1L;
        LocalDate previousEnd = current.getStartDate().minusDays(1L);
        LocalDate previousStart = previousEnd.minusDays(days - 1L);
        return new DashboardDateRange(previousStart, previousEnd,
                previousStart.atStartOfDay(effectiveZone).toInstant().toEpochMilli(),
                previousEnd.plusDays(1L).atStartOfDay(effectiveZone).toInstant().toEpochMilli(),
                current.getBucket(), false);
    }

    static List<TokenStatisticsUserUsage> consolidateUserUsage(List<TokenStatisticsUserUsage> rawUsers,
                                                               Map<String, CuihuaUserIdentity> identities) {
        Map<String, TokenStatisticsUserUsage> consolidated = new LinkedHashMap<String, TokenStatisticsUserUsage>();
        if (rawUsers == null) {
            return new ArrayList<TokenStatisticsUserUsage>();
        }
        Map<String, CuihuaUserIdentity> safeIdentities = identities == null
                ? Collections.<String, CuihuaUserIdentity>emptyMap() : identities;
        for (TokenStatisticsUserUsage raw : rawUsers) {
            if (raw == null) {
                continue;
            }
            String rawUserId = optionalText(raw.getUserId());
            CuihuaUserIdentity identity = rawUserId == null ? null : safeIdentities.get(rawUserId);
            String key = identity == null ? UNATTRIBUTED_KEY : rawUserId;
            TokenStatisticsUserUsage target = consolidated.get(key);
            if (target == null) {
                target = identity == null ? unattributedUsage() : attributedUsage(identity, rawUserId);
                consolidated.put(key, target);
            }
            target.addUsage(raw);
        }
        return new ArrayList<TokenStatisticsUserUsage>(consolidated.values());
    }

    static List<TokenStatisticsUserUsage> filterUserUsage(List<TokenStatisticsUserUsage> users, String search) {
        List<TokenStatisticsUserUsage> filtered = new ArrayList<TokenStatisticsUserUsage>();
        if (users == null) {
            return filtered;
        }
        String query = optionalText(search);
        if (query == null) {
            filtered.addAll(users);
            return filtered;
        }
        String lowered = query.toLowerCase(Locale.ROOT);
        for (TokenStatisticsUserUsage user : users) {
            if (user != null && (containsIgnoreCase(user.getUserDisplayName(), lowered)
                    || containsIgnoreCase(user.getUsername(), lowered)
                    || containsIgnoreCase(user.getUserId(), lowered)
                    || (user.isUnattributed() && "未归属用户".contains(query)))) {
                filtered.add(user);
            }
        }
        return filtered;
    }

    static Map<String, Set<String>> consolidateUserModels(List<TokenStatisticsUserModelUsage> rawModels,
                                                          Map<String, CuihuaUserIdentity> identities) {
        Map<String, Set<String>> modelsByUser = new LinkedHashMap<String, Set<String>>();
        if (rawModels == null) {
            return modelsByUser;
        }
        for (TokenStatisticsUserModelUsage raw : rawModels) {
            if (raw == null || optionalText(raw.getModel()) == null) {
                continue;
            }
            String key = canonicalUserKey(raw.getUserId(), identities);
            Set<String> models = modelsByUser.get(key);
            if (models == null) {
                models = new LinkedHashSet<String>();
                modelsByUser.put(key, models);
            }
            models.add(raw.getModel().trim());
        }
        return modelsByUser;
    }

    private static void applyModelCounts(List<TokenStatisticsUserUsage> users,
                                         Map<String, Set<String>> modelsByUser) {
        if (users == null) {
            return;
        }
        for (TokenStatisticsUserUsage user : users) {
            String key = user.isUnattributed() ? UNATTRIBUTED_KEY : user.getUserId();
            Set<String> models = modelsByUser.get(key);
            user.setModelCount(models == null ? 0L : models.size());
        }
    }

    private static long countDistinctModels(Map<String, Set<String>> modelsByUser, Set<String> selectedUserKeys) {
        Set<String> models = new LinkedHashSet<String>();
        if (selectedUserKeys != null) {
            for (String key : selectedUserKeys) {
                Set<String> userModels = modelsByUser.get(key);
                if (userModels != null) {
                    models.addAll(userModels);
                }
            }
        }
        return models.size();
    }

    private static List<TokenStatisticsUserUsage> pricedUsers(List<TokenStatisticsUserUsage> users) {
        List<TokenStatisticsUserUsage> priced = new ArrayList<TokenStatisticsUserUsage>();
        if (users != null) {
            for (TokenStatisticsUserUsage user : users) {
                if (user != null && user.getPricedRecordCount() > 0L) {
                    priced.add(user);
                }
            }
        }
        return priced;
    }

    static void sortUserUsage(List<TokenStatisticsUserUsage> users, String sort, String direction) {
        if (users == null || users.size() < 2) {
            return;
        }
        final String field = normalizeDashboardSort(sort);
        final boolean ascending = "asc".equalsIgnoreCase(direction);
        Collections.sort(users, new Comparator<TokenStatisticsUserUsage>() {
            @Override
            public int compare(TokenStatisticsUserUsage left, TokenStatisticsUserUsage right) {
                int comparison;
                if ("tokens".equals(field)) {
                    comparison = Long.compare(left.getTotalTokens(), right.getTotalTokens());
                } else if ("calls".equals(field)) {
                    comparison = Long.compare(left.getRequestCount(), right.getRequestCount());
                } else if ("models".equals(field)) {
                    comparison = Long.compare(left.getModelCount(), right.getModelCount());
                } else if ("average".equals(field)) {
                    comparison = Long.compare(averageTokens(left.getTotalTokens(), left.getRequestCount()),
                            averageTokens(right.getTotalTokens(), right.getRequestCount()));
                } else {
                    boolean leftPriced = left.getPricedRecordCount() > 0L;
                    boolean rightPriced = right.getPricedRecordCount() > 0L;
                    if (leftPriced != rightPriced) {
                        return leftPriced ? -1 : 1;
                    }
                    comparison = compareCost(left.getTotalCost(), right.getTotalCost());
                }
                if (!ascending) {
                    comparison = -comparison;
                }
                if (comparison != 0) {
                    return comparison;
                }
                int tokenFallback = Long.compare(right.getTotalTokens(), left.getTotalTokens());
                if (tokenFallback != 0) {
                    return tokenFallback;
                }
                return safeText(left.getUserDisplayName()).compareToIgnoreCase(safeText(right.getUserDisplayName()));
            }
        });
    }

    private static List<TokenStatisticsUserTrend> consolidateTrend(List<TokenStatisticsUserTrend> rawTrend,
                                                                  Map<String, CuihuaUserIdentity> identities,
                                                                  Set<String> selectedUserKeys) {
        Map<String, TokenStatisticsUserTrend> byBucket = new TreeMap<String, TokenStatisticsUserTrend>();
        if (rawTrend == null || selectedUserKeys == null || selectedUserKeys.isEmpty()) {
            return new ArrayList<TokenStatisticsUserTrend>();
        }
        for (TokenStatisticsUserTrend raw : rawTrend) {
            if (raw == null || optionalText(raw.getBucket()) == null) {
                continue;
            }
            String key = canonicalUserKey(raw.getUserId(), identities);
            if (!selectedUserKeys.contains(key)) {
                continue;
            }
            TokenStatisticsUserTrend aggregate = byBucket.get(raw.getBucket());
            if (aggregate == null) {
                aggregate = TokenStatisticsUserTrend.builder().bucket(raw.getBucket()).build();
                byBucket.put(raw.getBucket(), aggregate);
            }
            aggregate.addUsage(raw);
        }
        return new ArrayList<TokenStatisticsUserTrend>(byBucket.values());
    }

    private static Map<String, Object> summarizeUsers(List<TokenStatisticsUserUsage> users) {
        TokenStatisticsUserUsage total = TokenStatisticsUserUsage.builder().build();
        if (users != null) {
            for (TokenStatisticsUserUsage user : users) {
                total.addUsage(user);
            }
        }
        Map<String, Object> summary = new LinkedHashMap<String, Object>();
        summary.put("totalTokens", total.getTotalTokens());
        summary.put("totalPromptTokens", total.getPromptTokens());
        summary.put("totalCompletionTokens", total.getCompletionTokens());
        summary.put("recordCount", total.getRequestCount());
        summary.put("totalCost", total.getTotalCost());
        summary.put("currency", total.getCurrency());
        summary.put("pricedRecordCount", total.getPricedRecordCount());
        summary.put("unpricedRecordCount", total.getUnpricedRecordCount());
        return summary;
    }

    private static long averageTokens(long totalTokens, long requestCount) {
        return requestCount <= 0L ? 0L : Math.round((double) totalTokens / (double) requestCount);
    }

    private static Map<String, Object> costComparison(Map<String, Object> current,
                                                      Map<String, Object> previous) {
        Map<String, Object> comparison = new LinkedHashMap<String, Object>();
        if (previous == null) {
            comparison.put("type", "unavailable");
            comparison.put("percent", null);
            return comparison;
        }
        BigDecimal currentCost = decimalOrZero(current.get("totalCost"));
        BigDecimal previousCost = decimalOrZero(previous.get("totalCost"));
        if (previousCost.signum() == 0 && currentCost.signum() > 0) {
            comparison.put("type", "new");
            comparison.put("percent", null);
            return comparison;
        }
        comparison.put("type", "percent");
        if (previousCost.signum() == 0) {
            comparison.put("percent", BigDecimal.ZERO);
        } else {
            comparison.put("percent", currentCost.subtract(previousCost)
                    .multiply(new BigDecimal("100"))
                    .divide(previousCost, 2, RoundingMode.HALF_UP));
        }
        return comparison;
    }

    private static BigDecimal decimalOrZero(Object value) {
        return value instanceof BigDecimal ? (BigDecimal) value : BigDecimal.ZERO;
    }

    private static Set<String> collectUsageUserIds(List<TokenStatisticsUserUsage> users) {
        Set<String> userIds = new LinkedHashSet<String>();
        if (users != null) {
            for (TokenStatisticsUserUsage user : users) {
                if (user != null && optionalText(user.getUserId()) != null) {
                    userIds.add(user.getUserId().trim());
                }
            }
        }
        return userIds;
    }

    private static Set<String> collectCanonicalUserKeys(List<TokenStatisticsUserUsage> users) {
        Set<String> keys = new LinkedHashSet<String>();
        if (users != null) {
            for (TokenStatisticsUserUsage user : users) {
                if (user != null) {
                    keys.add(user.isUnattributed() ? UNATTRIBUTED_KEY : user.getUserId());
                }
            }
        }
        return keys;
    }

    private static String canonicalUserKey(String userId, Map<String, CuihuaUserIdentity> identities) {
        String normalized = optionalText(userId);
        return normalized != null && identities != null && identities.containsKey(normalized)
                ? normalized : UNATTRIBUTED_KEY;
    }

    private static TokenStatisticsUserUsage unattributedUsage() {
        return TokenStatisticsUserUsage.builder()
                .userDisplayName("未归属用户")
                .unattributed(true)
                .build();
    }

    private static TokenStatisticsUserUsage attributedUsage(CuihuaUserIdentity identity, String userId) {
        String username = optionalText(identity.getUsername());
        String displayName = optionalText(identity.getDisplayName());
        if (displayName == null) {
            displayName = username == null ? userId : username;
        }
        return TokenStatisticsUserUsage.builder()
                .userId(userId)
                .userDisplayName(displayName)
                .username(username)
                .build();
    }

    private static int compareCost(BigDecimal left, BigDecimal right) {
        if (left == null && right == null) {
            return 0;
        }
        if (left == null) {
            return -1;
        }
        if (right == null) {
            return 1;
        }
        return left.compareTo(right);
    }

    private static boolean containsIgnoreCase(String value, String loweredQuery) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(loweredQuery);
    }

    private static String normalizeDashboardSort(String sort) {
        return normalizeDashboardSort(sort, "cost");
    }

    private static String normalizeDashboardSort(String sort, String defaultSort) {
        if ("tokens".equalsIgnoreCase(sort)) {
            return "tokens";
        }
        if ("calls".equalsIgnoreCase(sort)) {
            return "calls";
        }
        if ("models".equalsIgnoreCase(sort)) {
            return "models";
        }
        if ("average".equalsIgnoreCase(sort)) {
            return "average";
        }
        if ("cost".equalsIgnoreCase(sort)) {
            return "cost";
        }
        return defaultSort;
    }

    static List<String> configuredPlatformTypes() {
        List<String> types = new ArrayList<String>();
        if (ContextLoader.configuration == null || ContextLoader.configuration.getModels() == null) {
            return types;
        }
        for (Backend model : ContextLoader.configuration.getModels()) {
            String type = model == null ? null : optionalText(model.getType());
            if (type != null && !containsIgnoreCase(types, type)) {
                types.add(type);
            }
        }
        return types;
    }

    private static boolean containsIgnoreCase(List<String> values, String candidate) {
        for (String value : values) {
            if (value.equalsIgnoreCase(candidate)) {
                return true;
            }
        }
        return false;
    }

    private static String resolvePlatform(String requested, List<String> configuredPlatforms) {
        String normalized = optionalText(requested);
        if (normalized == null) {
            return null;
        }
        if (OTHER_PLATFORM.equals(normalized)) {
            return OTHER_PLATFORM;
        }
        for (String configured : configuredPlatforms) {
            if (configured.equalsIgnoreCase(normalized)) {
                return configured;
            }
        }
        throw new IllegalArgumentException("unknown platform");
    }

    private static List<Map<String, String>> platformOptions(List<String> configuredPlatforms) {
        List<Map<String, String>> options = new ArrayList<Map<String, String>>();
        for (String configured : configuredPlatforms) {
            Map<String, String> option = new LinkedHashMap<String, String>();
            option.put("value", configured);
            option.put("label", configured);
            options.add(option);
        }
        Map<String, String> other = new LinkedHashMap<String, String>();
        other.put("value", OTHER_PLATFORM);
        other.put("label", "其他平台");
        options.add(other);
        return options;
    }

    private static String safeText(String value) {
        return value == null ? "" : value;
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

    static final class DashboardDateRange {
        private final LocalDate startDate;
        private final LocalDate endDate;
        private final long startMs;
        private final long endExclusiveMs;
        private final TokenStatisticsTrendBucket bucket;
        private final boolean allTime;

        private DashboardDateRange(LocalDate startDate, LocalDate endDate, long startMs, long endExclusiveMs,
                                   TokenStatisticsTrendBucket bucket, boolean allTime) {
            this.startDate = startDate;
            this.endDate = endDate;
            this.startMs = startMs;
            this.endExclusiveMs = endExclusiveMs;
            this.bucket = bucket;
            this.allTime = allTime;
        }

        LocalDate getStartDate() {
            return startDate;
        }

        LocalDate getEndDate() {
            return endDate;
        }

        long getStartMs() {
            return startMs;
        }

        long getEndExclusiveMs() {
            return endExclusiveMs;
        }

        TokenStatisticsTrendBucket getBucket() {
            return bucket;
        }

        boolean isAllTime() {
            return allTime;
        }
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

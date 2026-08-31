package ai.servlet.api;

import ai.account.CuihuaUserIdentity;
import ai.llm.pojo.TokenStatisticsDetail;
import ai.llm.pojo.TokenStatisticsTrendBucket;
import ai.llm.pojo.TokenStatisticsUserModelUsage;
import ai.llm.pojo.TokenStatisticsUserUsage;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TokenStatisticsApiServletTest {

    @Test
    void appliesResolvedNamesAndKeepsUserIdsAsTheFallback() {
        TokenStatisticsDetail named = TokenStatisticsDetail.builder().userId("usr_named").build();
        TokenStatisticsDetail unresolved = TokenStatisticsDetail.builder().userId("usr_unresolved").build();
        TokenStatisticsDetail blankName = TokenStatisticsDetail.builder().userId("usr_blank").build();
        TokenStatisticsDetail anonymous = TokenStatisticsDetail.builder().build();
        List<TokenStatisticsDetail> records = Arrays.asList(named, unresolved, blankName, anonymous);
        Map<String, String> displayNames = new HashMap<String, String>();
        displayNames.put("usr_named", "当前姓名");
        displayNames.put("usr_blank", "   ");

        TokenStatisticsApiServlet.applyUserDisplayNames(records, displayNames);

        assertEquals("当前姓名", named.getUserDisplayName());
        assertEquals("usr_unresolved", unresolved.getUserDisplayName());
        assertEquals("usr_blank", blankName.getUserDisplayName());
        assertNull(anonymous.getUserDisplayName());

        TokenStatisticsApiServlet.applyUserDisplayNames(records, Collections.<String, String>emptyMap());
        assertEquals("usr_named", named.getUserDisplayName());
    }

    @Test
    void customDateRangeIncludesBothCalendarDaysAndSelectsAdaptiveBuckets() {
        ZoneId zone = ZoneId.of("Asia/Shanghai");
        TokenStatisticsApiServlet.DashboardDateRange oneDay = TokenStatisticsApiServlet
                .parseDashboardDateRange(null, "2026-08-27", "2026-08-27", zone);
        TokenStatisticsApiServlet.DashboardDateRange ninetyDays = TokenStatisticsApiServlet
                .parseDashboardDateRange(null, "2026-06-01", "2026-08-29", zone);
        TokenStatisticsApiServlet.DashboardDateRange longRange = TokenStatisticsApiServlet
                .parseDashboardDateRange(null, "2026-01-01", "2026-08-29", zone);

        assertEquals(TokenStatisticsTrendBucket.HOUR, oneDay.getBucket());
        assertEquals(LocalDate.of(2026, 8, 27), oneDay.getStartDate());
        assertEquals(LocalDate.of(2026, 8, 27), oneDay.getEndDate());
        assertEquals(24L * 60L * 60L * 1000L, oneDay.getEndExclusiveMs() - oneDay.getStartMs());
        assertEquals(TokenStatisticsTrendBucket.DAY, ninetyDays.getBucket());
        assertEquals(TokenStatisticsTrendBucket.MONTH, longRange.getBucket());
    }

    @Test
    void allRangeUsesMonthlyBucketsWithoutSyntheticDates() {
        TokenStatisticsApiServlet.DashboardDateRange range = TokenStatisticsApiServlet
                .parseDashboardDateRange("all", null, null, ZoneId.of("Asia/Shanghai"));

        assertTrue(range.isAllTime());
        assertEquals(0L, range.getStartMs());
        assertEquals(Long.MAX_VALUE, range.getEndExclusiveMs());
        assertEquals(TokenStatisticsTrendBucket.MONTH, range.getBucket());
        assertNull(range.getStartDate());
        assertNull(range.getEndDate());
        assertNull(TokenStatisticsApiServlet.previousDashboardDateRange(range, ZoneId.of("Asia/Shanghai")));
    }

    @Test
    void previousRangeIsTheImmediatelyPrecedingEqualLengthPeriod() {
        ZoneId zone = ZoneId.of("Asia/Shanghai");
        TokenStatisticsApiServlet.DashboardDateRange current = TokenStatisticsApiServlet
                .parseDashboardDateRange(null, "2026-08-27", "2026-08-29", zone);

        TokenStatisticsApiServlet.DashboardDateRange previous = TokenStatisticsApiServlet
                .previousDashboardDateRange(current, zone);

        assertEquals(LocalDate.of(2026, 8, 24), previous.getStartDate());
        assertEquals(LocalDate.of(2026, 8, 26), previous.getEndDate());
        assertEquals(current.getEndExclusiveMs() - current.getStartMs(),
                previous.getEndExclusiveMs() - previous.getStartMs());
    }

    @Test
    void consolidatesMissingAccountsIntoOneUnattributedUsageRow() {
        List<TokenStatisticsUserUsage> raw = Arrays.asList(
                usage("usr_current", 2L, 200L, new BigDecimal("1.20"), 2L, 0L),
                usage("usr_deleted", 3L, 300L, new BigDecimal("2.30"), 2L, 1L),
                usage(null, 4L, 400L, null, 0L, 4L));
        Map<String, CuihuaUserIdentity> identities = new HashMap<String, CuihuaUserIdentity>();
        identities.put("usr_current", CuihuaUserIdentity.builder()
                .userId("usr_current")
                .displayName("当前姓名")
                .username("current-login")
                .build());

        List<TokenStatisticsUserUsage> consolidated = TokenStatisticsApiServlet
                .consolidateUserUsage(raw, identities);

        assertEquals(2, consolidated.size());
        TokenStatisticsUserUsage current = consolidated.stream()
                .filter(item -> "usr_current".equals(item.getUserId()))
                .findFirst().get();
        TokenStatisticsUserUsage unattributed = consolidated.stream()
                .filter(TokenStatisticsUserUsage::isUnattributed)
                .findFirst().get();
        assertEquals("当前姓名", current.getUserDisplayName());
        assertEquals("current-login", current.getUsername());
        assertEquals(7L, unattributed.getRequestCount());
        assertEquals(700L, unattributed.getTotalTokens());
        assertEquals(5L, unattributed.getUnpricedRecordCount());
        assertEquals(0, new BigDecimal("2.30").compareTo(unattributed.getTotalCost()));
    }

    @Test
    void filtersAllIdentityFieldsAndSortsPricedCostBeforeUnknownCost() {
        TokenStatisticsUserUsage named = usage("usr_named", 5L, 500L, new BigDecimal("1.00"), 5L, 0L);
        named.setUserDisplayName("张三");
        named.setUsername("zhangsan");
        TokenStatisticsUserUsage costly = usage("usr_costly", 2L, 200L, new BigDecimal("9.00"), 2L, 0L);
        costly.setUserDisplayName("李四");
        costly.setUsername("lisi");
        TokenStatisticsUserUsage unknownCost = usage("usr_unknown", 9L, 900L, null, 0L, 9L);
        unknownCost.setUserDisplayName("王五");
        unknownCost.setUsername("wangwu");
        List<TokenStatisticsUserUsage> rows = Arrays.asList(named, unknownCost, costly);

        assertEquals(1, TokenStatisticsApiServlet.filterUserUsage(rows, "zhang").size());
        assertEquals(1, TokenStatisticsApiServlet.filterUserUsage(rows, "usr_costly").size());

        List<TokenStatisticsUserUsage> sorted = TokenStatisticsApiServlet.filterUserUsage(rows, null);
        TokenStatisticsApiServlet.sortUserUsage(sorted, "cost", "desc");
        assertEquals("usr_costly", sorted.get(0).getUserId());
        assertEquals("usr_named", sorted.get(1).getUserId());
        assertEquals("usr_unknown", sorted.get(2).getUserId());
    }

    @Test
    void unionsDistinctModelsAfterMissingUsersAreConsolidated() {
        Map<String, CuihuaUserIdentity> identities = new HashMap<String, CuihuaUserIdentity>();
        identities.put("usr_current", CuihuaUserIdentity.builder().userId("usr_current").build());
        List<TokenStatisticsUserModelUsage> raw = Arrays.asList(
                model("usr_current", "model-a"), model("usr_current", "model-b"),
                model("usr_deleted", "model-a"), model(null, "model-b"),
                model("usr_deleted", "model-c"), model("usr_deleted", "  "));

        Map<String, Set<String>> consolidated = TokenStatisticsApiServlet.consolidateUserModels(raw, identities);

        assertEquals(2, consolidated.get("usr_current").size());
        assertEquals(3, consolidated.get("__unattributed__").size());
    }

    @Test
    void usageRowsSortByModelCountAndRoundedAverageTokens() {
        TokenStatisticsUserUsage first = usage("first", 4L, 100L, null, 0L, 4L);
        first.setModelCount(1L);
        TokenStatisticsUserUsage second = usage("second", 2L, 90L, null, 0L, 2L);
        second.setModelCount(3L);
        List<TokenStatisticsUserUsage> rows = Arrays.asList(first, second);

        TokenStatisticsApiServlet.sortUserUsage(rows, "models", "desc");
        assertEquals("second", rows.get(0).getUserId());
        TokenStatisticsApiServlet.sortUserUsage(rows, "average", "asc");
        assertEquals("first", rows.get(0).getUserId());
    }

    private TokenStatisticsUserUsage usage(String userId, long calls, long totalTokens, BigDecimal totalCost,
                                           long pricedCalls, long unpricedCalls) {
        return TokenStatisticsUserUsage.builder()
                .userId(userId)
                .requestCount(calls)
                .promptTokens(totalTokens - 10L)
                .completionTokens(10L)
                .totalTokens(totalTokens)
                .totalCost(totalCost)
                .currency(totalCost == null ? null : "CNY")
                .pricedRecordCount(pricedCalls)
                .unpricedRecordCount(unpricedCalls)
                .build();
    }

    private TokenStatisticsUserModelUsage model(String userId, String model) {
        return TokenStatisticsUserModelUsage.builder().userId(userId).model(model).build();
    }
}

package ai.servlet;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

class TokenUsageFrontendContractTest {

    @Test
    void userColumnPrefersResolvedDisplayNameAndFallsBackToUserId() throws Exception {
        String source = readWebAsset("tokenUsage.js");
        String picker = functionBlock(source, "function pickTokenUsageUserName");
        String renderer = functionBlock(source, "function renderTokenUserTable");

        assertTrue(picker.contains("row.userDisplayName"));
        assertTrue(picker.contains("row.username"));
        assertTrue(picker.contains("row.userId"));
        assertTrue(renderer.contains("pickTokenUsageUserName(row)"));
        assertTrue(renderer.contains("escapeTokenUsageHtml(userName)"));
    }

    @Test
    void rendersSiblingCostAndUsagePagesWithSharedFilters() throws Exception {
        String source = readWebAsset("tokenUsage.js");
        String nav = readWebAsset("nav.js");

        assertTrue(source.contains("/v1/token-statistics/dashboard"));
        assertTrue(source.contains("function loadCostOverviewPage"));
        assertTrue(source.contains("function loadUsageStatisticsPage"));
        assertTrue(source.contains("查看平台和用户的估算费用及变化趋势"));
        assertTrue(source.contains("查看平台和用户的模型调用与 Token 消耗"));
        assertTrue(source.contains("本期估算费用"));
        assertTrue(source.contains("上期估算费用"));
        assertTrue(source.contains("环比变化"));
        assertTrue(source.contains("调用模型数"));
        assertTrue(source.contains("调用成功总数"));
        assertTrue(source.contains("平均单次请求 Token 量"));
        assertTrue(source.contains("未归属用户"));
        assertTrue(source.contains("tokenDateStart"));
        assertTrue(source.contains("tokenDateEnd"));
        assertTrue(source.contains("tokenPlatform"));
        assertTrue(source.contains("tokenUserSearch"));
        assertTrue(source.contains("tokenSharedFilters"));
        assertTrue(nav.contains("title: '费用概览'"));
        assertTrue(nav.contains("title: '用量统计'"));
        assertTrue(nav.contains("loadCostOverviewPage()"));
        assertTrue(nav.contains("loadUsageStatisticsPage()"));
        assertFalse(source.contains("模型用量分布"));
        assertFalse(source.contains("含零用量"));
        assertFalse(source.contains("tokenMetricSelect"));
    }

    @Test
    void dashboardEndpointIsMappedToTokenStatisticsServlet() throws Exception {
        Path path = Paths.get("src", "main", "webapp", "WEB-INF", "web.xml");
        String source = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);

        assertTrue(source.contains("<url-pattern>/v1/token-statistics/dashboard</url-pattern>"));
    }

    @Test
    void initializationEndpointIsMappedToCuihuaIntegrationServlet() throws Exception {
        Path path = Paths.get("src", "main", "webapp", "WEB-INF", "web.xml");
        String source = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);

        assertTrue(source.contains("<url-pattern>/v1/integration/initialize</url-pattern>"));
    }

    @Test
    void costChartIsCostOnlyWhileUsageChartUsesCallsAndTokensOnTwoAxes() throws Exception {
        String source = readWebAsset("tokenUsage.js");
        String costChart = functionBlock(source, "function buildCostChartOption");
        String usageChart = functionBlock(source, "function buildUsageChartOption");

        assertTrue(costChart.contains("估算费用"));
        assertTrue(costChart.contains("type: 'line'"));
        assertTrue(costChart.contains("unpricedRecordCount"));
        assertFalse(costChart.contains("yAxisIndex"));
        assertFalse(costChart.contains("totalTokens"));
        assertTrue(usageChart.contains("调用成功数"));
        assertTrue(usageChart.contains("Token"));
        assertFalse(usageChart.contains("估算费用"));
        assertTrue(usageChart.contains("type: 'bar'"));
        assertTrue(usageChart.contains("type: 'line'"));
        assertTrue(usageChart.contains("yAxisIndex: 0"));
        assertTrue(usageChart.contains("yAxisIndex: 1"));
        assertTrue(source.contains("end.setHours(23, 0, 0, 0)"));
    }

    private String readWebAsset(String name) throws Exception {
        Path path = Paths.get("src", "main", "webapp", "js", name);
        return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
    }

    private String functionBlock(String source, String startMarker) {
        int start = source.indexOf(startMarker);
        assertTrue(start >= 0, "Missing marker: " + startMarker);
        int bodyStart = source.indexOf('{', start + startMarker.length());
        assertTrue(bodyStart > start, "Missing function body: " + startMarker);
        int depth = 0;
        for (int i = bodyStart; i < source.length(); i++) {
            char current = source.charAt(i);
            if (current == '{') {
                depth++;
            } else if (current == '}' && --depth == 0) {
                return source.substring(start, i + 1);
            }
        }
        throw new AssertionError("Unclosed function body: " + startMarker);
    }
}

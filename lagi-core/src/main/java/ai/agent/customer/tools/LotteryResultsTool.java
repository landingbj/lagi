package ai.agent.customer.tools;

import ai.agent.customer.pojo.ToolArg;
import ai.agent.customer.pojo.ToolInfo;
import ai.utils.ApiInvokeUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.Setter;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Setter
public class LotteryResultsTool extends AbstractTool {

    private static final String API_ADDRESS = "https://api.pearktrue.cn/api/lottery/";

    public LotteryResultsTool() {
        init();
    }

    private void init() {
        name = "lottery_results";
        toolInfo = ToolInfo.builder().name("lottery_results")
                .description("通过API查询彩票结果")
                .args(java.util.Arrays.asList(
                        ToolArg.builder()
                                .name("get").type("string").description("彩票类型,包括（快乐8、双色球、大乐透、福彩3D、排列3、排列5、七乐彩、7星彩、胜负彩、进球彩、半全场）,彩票的缩写（kl8、ssq、dlt、fc3d、pl3、pl5、qlc、qxc、sfc、jqc、bqc）")
                                .build(),
                        ToolArg.builder()
                                .name("num").type("int").description("查询天数，默认为30")
                                .build()))
                .build();
        register(this);
    }

    public String getLotteryResults(String lotteryType, int numDays) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("get", lotteryType);
        queryParams.put("num", String.valueOf(numDays));

        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");

        String response = ApiInvokeUtil.get(API_ADDRESS, queryParams, headers, 15, java.util.concurrent.TimeUnit.SECONDS);

        if (response == null) {
            return "查询失败，未获得响应数据";
        }

        Gson gson = new Gson();
        Type typeResponse = new TypeToken<Map<String, Object>>() {}.getType();
        Map<String, Object> responseData = gson.fromJson(response, typeResponse);

        if (responseData == null || responseData.get("code") == null) {
            return "查询失败，返回数据无效";
        }

        Object codeObj = responseData.get("code");
        if (codeObj instanceof Double && ((Double) codeObj).intValue() != 200) {
            return "查询失败，返回状态不正常";
        }

        List<Map<String, String>> results = (List<Map<String, String>>) responseData.get("data");

        if (results == null || results.isEmpty()) {
            return "未找到相关彩票结果";
        }

        StringBuilder resultBuilder = new StringBuilder();
        for (Map<String, String> result : results) {
            resultBuilder.append("期号: ").append(result.get("issue")).append("\n")
                    .append("开奖时间: ").append(result.get("opentime")).append("\n")
                    .append("销售金额: ").append(result.get("salemoney")).append("\n")
                    .append("中奖号码: ").append(result.get("drawnumber")).append("\n")
                    .append("追加号码: ").append(result.get("trailnumber")).append("\n\n");
        }

        return resultBuilder.toString();
    }

    @Override
    public String apply(Map<String, Object> args) {
        String lotteryType = (String) args.get("get");
        int numDays = args.containsKey("num") ? (int) args.get("num") : 30;
        return getLotteryResults(lotteryType, numDays);
    }

    public static void main(String[] args) {
        LotteryResultsTool lotteryResultsTool = new LotteryResultsTool();
        String result = lotteryResultsTool.getLotteryResults("ssq", 7);
        System.out.println(result);
    }
}

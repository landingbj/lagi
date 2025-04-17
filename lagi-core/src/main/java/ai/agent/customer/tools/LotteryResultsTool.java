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
//    https://api.pearktrue.cn/api/lottery/?get=ssq

    public LotteryResultsTool() {
        init();
    }

    private void init() {
        name = "lottery_results";
        toolInfo = ToolInfo.builder().name("lottery_results")
                .description("通过API查询彩票结果")
                .args(java.util.Arrays.asList(
                        ToolArg.builder()
                                .name("type").type("string").description("彩票类型的缩写形式, 彩票全程缩写对应关系如下 :\n 快乐8:kl8\n双色球:ssq\n大乐透:dlt\n福彩3D:fc3d\n排列3:pl3\n排列5:pl5\n七乐彩:qlc\n7星彩:qxc\n胜负彩:sfc\n、进球彩:jqc\n半全场:bqc")
                                .build(),
                        ToolArg.builder()
                                .name("num").type("int").description("查询天数，默认为1")
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
        resultBuilder.append("彩票开奖结果如下：\n");
        resultBuilder.append("|期号").append("|开奖时间").append("|销售金额").append("|中奖号码").append("|追加号码|").append(" \n");
        resultBuilder.append("|-----|-----|-----|-----|-----| \n");
        for (Map<String, String> result : results) {
            resultBuilder.append("|").append(result.get("issue"))
                    .append("|").append(result.get("opentime"))
                    .append("|").append(result.get("salemoney"))
                    .append("|").append(result.get("drawnumber"))
                    .append("|").append(result.get("trailnumber"))
                    .append("| \n");
        }
        return resultBuilder.toString();
    }

    @Override
    public String apply(Map<String, Object> args) {
        String lotteryType = (String) args.get("type");
        int numDays = args.containsKey("num") ? ((Double)args.get("num")).intValue() : 30;
        return getLotteryResults(lotteryType, numDays);
    }

    public static void main(String[] args) {
        LotteryResultsTool lotteryResultsTool = new LotteryResultsTool();
        String result = lotteryResultsTool.getLotteryResults("ssq", 7);
        System.out.println(result);
    }
}

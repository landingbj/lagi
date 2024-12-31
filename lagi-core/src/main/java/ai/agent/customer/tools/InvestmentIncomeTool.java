package ai.agent.customer.tools;

import ai.agent.customer.pojo.ToolArg;
import ai.agent.customer.pojo.ToolInfo;
import ai.utils.ApiInvokeUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.Setter;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

@Setter
public class InvestmentIncomeTool extends AbstractTool {

    private static final String API_ADDRESS = "https://api.pearktrue.cn/api/investmentincome/";

    public InvestmentIncomeTool() {
        init();
    }

    private void init() {
        name = "investment_income";
        toolInfo = ToolInfo.builder().name("investment_income")
                .description("这是一个根据用户的投资信息计算收益的工具")
                .args(java.util.Arrays.asList(
                        ToolArg.builder()
                                .name("principal").type("float").description("投资本金")
                                .build(),
                        ToolArg.builder()
                                .name("annualRate").type("float").description("年化收益率")
                                .build(),
                        ToolArg.builder()
                                .name("duration").type("int").description("投资期限")
                                .build(),
                        ToolArg.builder()
                                .name("type").type("string").description("投资类型（如：day, month, year）")
                                .build()))
                .build();
        register(this);
    }

    public String getInvestmentIncome(float principal, float annualRate, int duration, String type) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("principal", String.valueOf(principal));
        queryParams.put("annualRate", String.valueOf(annualRate));
        queryParams.put("duration", String.valueOf(duration));
        queryParams.put("type", type);

        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");

        String response = ApiInvokeUtil.get(API_ADDRESS, queryParams, headers, 15, java.util.concurrent.TimeUnit.SECONDS);

        if (response == null) {
            return "计算失败";
        }

        Gson gson = new Gson();
        Type typeResponse = new TypeToken<Map<String, Object>>() {}.getType();
        Map<String, Object> responseData = gson.fromJson(response, typeResponse);

        if (responseData == null || responseData.get("code") == null) {
            return "计算失败";
        }

        Object codeObj = responseData.get("code");
        if (codeObj instanceof Double && ((Double) codeObj).intValue() != 200) {
            return "计算失败";
        }

        String returnMoney = (String) responseData.get("returnmoney");
        String totalMoney = (String) responseData.get("totalmoney");

        return String.format("预计收益: %s\n总金额: %s", returnMoney, totalMoney);
    }

    @Override
    public String apply(Map<String, Object> args) {
        float principal = ((Number) args.get("principal")).floatValue();
        float annualRate = ((Number) args.get("annualRate")).floatValue();
        int duration = ((Number) args.get("duration")).intValue();
        String type = (String) args.get("type");

        return getInvestmentIncome(principal, annualRate, duration, type);
    }

    public static void main(String[] args) {
        InvestmentIncomeTool investmentIncomeTool = new InvestmentIncomeTool();
        String result = investmentIncomeTool.getInvestmentIncome(2432032, 3.425f, 5, "day");
        System.out.println(result);
    }
}

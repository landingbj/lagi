package ai.agent.customer.tools;

import ai.agent.customer.pojo.ToolArg;
import ai.agent.customer.pojo.ToolInfo;
import ai.utils.ApiInvokeUtil;
import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import lombok.Setter;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Setter
public class TrademarkInfoTool extends AbstractTool {

    private static final String API_ADDRESS = "https://api.pearktrue.cn/api/trademark/";

    public TrademarkInfoTool() {
        init();
    }

    private void init() {
        name = "trademark_info";
        toolInfo = ToolInfo.builder().name("trademark_info")
                .description("这是一个商标信息查询工具，可以查询商标的注册号、申请日期、代理机构等信息")
                .args(Lists.newArrayList(
                        ToolArg.builder()
                                .name("keyword").type("string").description("需要查询的商标名称，如 '哇哈哈'")
                                .build()))
                .build();
        register(this);
    }

    public String getTrademarkInfo(String keyword) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("keyword", keyword);
        String response = ApiInvokeUtil.get(API_ADDRESS, queryParams, headers, 15, TimeUnit.SECONDS);

        if (response == null) {
            return "查询失败";
        }

        Gson gson = new Gson();
        Type type = new TypeToken<Map<String, Object>>() {}.getType();
        Map<String, Object> map = gson.fromJson(response, type);

        if (map == null || map.get("code") == null) {
            return "查询失败";
        }

        Object codeObj = map.get("code");
        if (codeObj instanceof Double && ((Double) codeObj).intValue() != 200) {
            return "查询失败";
        }

        List<Map<String, Object>> trademarks = (List<Map<String, Object>>) map.get("data");

        StringBuilder result = new StringBuilder();
        for (Map<String, Object> trademark : trademarks) {
            String regNo = (String) trademark.get("regNo");
            String agent = (String) trademark.get("agent");
            String regDate = (String) trademark.get("regDate");
            String appDate = (String) trademark.get("appDate");
            String status = (String) trademark.get("statusStr");
            String intCls = (String) trademark.get("intCls");
            String clsStr = (String) trademark.get("clsStr");
            String applicantCn = (String) trademark.get("applicantCn");
            String tmName = (String) trademark.get("tmName");
            String tmImgOssPath = (String) trademark.get("tmImgOssPath");

            result.append(StrUtil.format(
                    "商标名称: {}\n注册号: {}\n代理机构: {}\n注册日期: {}\n申请日期: {}\n商标状态: {}\n国际分类: {}\n分类: {}\n申请人: {}\n商标图片: {}\n\n",
                    tmName, regNo, agent, regDate, appDate, status, intCls, clsStr, applicantCn, tmImgOssPath));
        }

        return result.toString();
    }

    @Override
    public String apply(Map<String, Object> args) {
        String keyword = (String) args.get("keyword");
        return getTrademarkInfo(keyword);
    }

    public static void main(String[] args) {
        TrademarkInfoTool trademarkInfoTool = new TrademarkInfoTool();
        String result = trademarkInfoTool.getTrademarkInfo("哇哈哈");
        System.out.println(result);
    }
}

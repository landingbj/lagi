package ai.agent.customer.tools;

import ai.agent.customer.pojo.ToolArg;
import ai.agent.customer.pojo.ToolInfo;
import ai.utils.ApiInvokeUtil;
import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.Setter;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Setter
public class HistoricalFigureInfoTool extends AbstractTool {

    private static final String API_ADDRESS = "https://api.pearktrue.cn/api/yearsmap/";

    public HistoricalFigureInfoTool() {
        init();
    }

    private void init() {
        name = "historical_figure_info";
        toolInfo = ToolInfo.builder().name("historical_figure_info")
                .description("这是一个查询历史人物信息的工具，可以帮助用户获取历史人物的详细信息")
                .args(Lists.newArrayList(
                        ToolArg.builder()
                                .name("name").type("string").description("想要查询的历史人物名称")
                                .build()))
                .build();
        register(this);
    }

    public String getHistoricalFigureInfo(String name) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("name", name);
        String response = ApiInvokeUtil.get(API_ADDRESS, queryParams, headers, 15, TimeUnit.SECONDS);

        if (response == null) {
            return "查询失败";
        }

        Gson gson = new Gson();
        Type type = new TypeToken<Map<String, Object>>(){}.getType();
        Map<String, Object> map = gson.fromJson(response, type);

        if (map == null || map.get("code") == null || ((Double) map.get("code")).intValue() != 200) {
            return "查询失败";
        }

        List<Map<String, Object>> dataList = (List<Map<String, Object>>) map.get("data");
        if (dataList == null || dataList.isEmpty()) {
            return "没有找到相关历史人物信息";
        }

        Map<String, Object> data = dataList.get(0);
        String nameField = (String) data.get("name");
        String dynasty = (String) data.get("dynasty");
        String desc = (String) data.get("desc");
        StringBuilder infoBuilder = new StringBuilder();
        for (String info : (List<String>) data.get("info")) {
            infoBuilder.append(info).append("\n");
        }

        return StrUtil.format("人物名称: {}\n朝代: {}\n简介: {}\n主要信息:\n{}", nameField, dynasty, desc, infoBuilder.toString());
    }

    @Override
    public String apply(Map<String, Object> args) {
        String name = (String) args.get("name");
        return getHistoricalFigureInfo(name);
    }

    public static void main(String[] args) {
        HistoricalFigureInfoTool tool = new HistoricalFigureInfoTool();
        String result = tool.getHistoricalFigureInfo("曹操");
        System.out.println(result);
    }
}

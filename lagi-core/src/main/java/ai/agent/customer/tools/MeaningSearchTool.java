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
public class MeaningSearchTool extends AbstractTool {

    private static final String API_ADDRESS = "https://api.pearktrue.cn/api/meansearch/";

    public MeaningSearchTool() {
        init();
    }

    private void init() {
        name = "meaning_search";
        toolInfo = ToolInfo.builder().name("meaning_search")
                .description("根据意思查找对应诗词或名言的工具")
                .args(Lists.newArrayList(ToolArg.builder().name("mean").type("String").build(),
                        ToolArg.builder().name("type").type("String").build()))
                .build();
        register(this);
    }

    public String searchQuotes(String mean, String type) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("mean", mean);
        queryParams.put("type", type);

        String response = ApiInvokeUtil.get(API_ADDRESS, queryParams, headers, 15, TimeUnit.SECONDS);
        if (response == null) {
            return "获取失败";
        }

        Gson gson = new Gson();
        Type typeMap = new TypeToken<Map<String, Object>>() {}.getType();
        Map<String, Object> map = gson.fromJson(response, typeMap);

        if (map == null || map.get("code") == null) {
            return "获取失败";
        }

        Object codeObj = map.get("code");
        if (codeObj instanceof Double && ((Double) codeObj).intValue() != 200) {
            return "获取失败";
        }

        List<Map<String, String>> quotes = (List<Map<String, String>>) map.get("data");
        if (quotes == null || quotes.isEmpty()) {
            return "未找到相关名言或诗词";
        }

        StringBuilder result = new StringBuilder("根据意思查找到的名言或诗句:\n");
        for (Map<String, String> quote : quotes) {
            String quoteText = quote.get("quote");
            String source = quote.get("source");
            result.append(StrUtil.format("“{}” —— {}\n", quoteText, source));
        }
        return result.toString();
    }

    @Override
    public String apply(Map<String, Object> args) {
        String mean = (String) args.get("mean");
        String type = (String) args.get("type");
        return searchQuotes(mean, type);
    }

    public static void main(String[] args) {
        MeaningSearchTool meaningSearchTool = new MeaningSearchTool();
        String result = meaningSearchTool.searchQuotes("树立远大志向", "现代文");
        System.out.println(result);
    }
}

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
public class RecipeQueryTool extends AbstractTool {

    private static final String API_ADDRESS = "https://api.pearktrue.cn/api/cookbook/";

    public RecipeQueryTool() {
        init();
    }

    private void init() {
        name = "recipe_query";
        toolInfo = ToolInfo.builder().name("recipe_query")
                .description("这是一个菜谱查询工具，可以根据菜名查询菜谱信息，包括简介、材料和做法")
                .args(Lists.newArrayList(
                        ToolArg.builder()
                                .name("search").type("string").description("菜谱查询的菜名")
                                .build()))
                .build();
        register(this);
    }

    public String getRecipeInfo(String search) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("search", search);
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

        List<Map<String, Object>> data = (List<Map<String, Object>>) map.get("data");
        if (data == null || data.isEmpty()) {
            return "没有找到相关菜谱";
        }

        Map<String, Object> recipe = data.get(0);
        String name = (String) recipe.get("name");
        String image = (String) recipe.get("image");
        String description = (String) recipe.get("description");
        List<String> materials = (List<String>) recipe.get("materials");
        List<String> practice = (List<String>) recipe.get("practice");

        StringBuilder result = new StringBuilder();
        result.append("菜谱名称：").append(name).append("\n")
              .append("图片：").append(image).append("\n")
              .append("简介：").append(description).append("\n")
              .append("材料：\n");
        
        for (String material : materials) {
            result.append("- ").append(material).append("\n");
        }

        result.append("做法：\n");
        for (String step : practice) {
            result.append("- ").append(step).append("\n");
        }

        return StrUtil.format("查询结果：\n{}", result.toString());
    }

    @Override
    public String apply(Map<String, Object> args) {
        String search = (String) args.get("search");
        return getRecipeInfo(search);
    }

    public static void main(String[] args) {
        RecipeQueryTool recipeQueryTool = new RecipeQueryTool();
        String result = recipeQueryTool.getRecipeInfo("家常红烧鱼块");
        System.out.println(result);
    }
}

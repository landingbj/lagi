package ai.agent.customer.tools;

import ai.agent.customer.pojo.ToolArg;
import ai.agent.customer.pojo.ToolInfo;
import ai.utils.ApiInvokeUtil;
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
public class FoodCalorieTool extends AbstractTool {

    private static final String API_ADDRESS = "https://api.istero.com/resource/food/calorie/query";
    private String token = "";

    public FoodCalorieTool() {
        init();
    }

    private void init() {
        name = "food_calorie_tool";
        toolInfo = ToolInfo.builder().name("food_calorie_tool")
                .description("这是食物热量查询工具可以用过输入的食物查询相应食物所热量")
                .args(Lists.newArrayList(
                        ToolArg.builder().name("food").type("string").description("食物").build()
                        ))
                .build();
        register(this);
    }

    public FoodCalorieTool(String token) {
        this.token = token;
        init();
    }

    private String search(String food) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Authorization", token);
        Map<String, Object> body = new HashMap<>();
        body.put("food", food);
        Gson gson = new Gson();
        String post = ApiInvokeUtil.post(API_ADDRESS, headers, gson.toJson(body), 15, TimeUnit.SECONDS);
        Type type = new TypeToken<Map<String, Object>>(){}.getType();
        Map<String, Object> map = gson.fromJson(post, type);
        if (map == null) {
            return "查询失败";
        }
        Object o = map.get("data");
        List<List<Map<String, String>>> listOfMaps = (List<List<Map<String, String>>>) o;
        Map<String, String> map1 = listOfMaps.get(0).get(0);
        map1.put("food", map1.get("name"));
        map1.put("热量/卡路里", map1.get("calorie"));
        map1.remove("name");
        map1.remove("calorie");
        return gson.toJson(map1);
    }


    @Override
    public String apply(Map<String, Object> args) {
        String food = (String) args.get("food");
        return search(food);
    }


}

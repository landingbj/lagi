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
import java.util.Map;
import java.util.concurrent.TimeUnit;


@Setter
public class BMITool extends AbstractTool {

    private static final String API_ADDRESS = "https://api.istero.com/resource/v1/bmi/query";
    private String token = "";

    public BMITool() {
        init();
    }

    private void init() {
        name = "bmi_calculation_tool";
        toolInfo = ToolInfo.builder().name("bmi_calculation_tool")
                .description("这是一个身体质量指数(BMI)查询工具可以通过输入性别年龄身高体等信息计算BMI值")
                .args(Lists.newArrayList(
                        ToolArg.builder().name("sex").type("string").description("性别：男 女").build(),
                        ToolArg.builder().name("age").type("int").description("年龄: 岁").build(),
                        ToolArg.builder().name("weight").type("int").description("体重: kg").build(),
                        ToolArg.builder().name("height").type("int").description("身高: cm").build()
                        ))
                .build();
        register(this);
    }

    public BMITool(String token) {
        this.token = token;
        init();
    }

//    private String search(String sex, Integer age, Integer weight, Integer height) {
//        Map<String, String> headers = new HashMap<>();
//        headers.put("Content-Type", "application/json");
//        headers.put("Authorization", "Bearer " + token);
//        Map<String, Object> body = new HashMap<>();
//        if(sex != null) {
//            body.put("sex", sex);
//        }
//        if(age != null) {
//            body.put("age", age);
//        }
//        if(weight != null) {
//            body.put("weight", weight);
//        }
//        if(height != null) {
//            body.put("height", height);
//        }
//        Gson gson = new Gson();
//        String post = ApiInvokeUtil.post(API_ADDRESS, headers, gson.toJson(body), 15, TimeUnit.SECONDS);
//        Type type = new TypeToken<Map<String, Object>>(){}.getType();
//        Map<String, Object> map = gson.fromJson(post, type);
//        if (map == null) {
//            return "查询失败";
//        }
//        Map<String, Object> data  = (Map<String, Object>)map.get("data");
//        data.put("BMI值", data.get("bmi").toString());
//        data.remove("bmi");
//        data.put("体重范围", data.get("interpretation").toString());
//        data.remove("interpretation");
//        return gson.toJson(data);
//    }
private String search(String sex, Integer age, Integer weight, Integer height) {
    if (weight == null || height == null) {
        return "查询失败：体重和身高为必填项";
    }
    if (height <= 0) {
        return "查询失败：身高必须大于0";
    }

    // 计算BMI值 (kg/m²)
    double heightInMeters = height / 100.0; // 厘米转米
    double bmi = weight / (heightInMeters * heightInMeters);

    // 根据BMI值判断体重范围（参考WHO标准）
    String interpretation;
    if (bmi < 18.5) {
        interpretation = "体重过轻";
    } else if (bmi < 24) {
        interpretation = "正常范围";
    } else if (bmi < 28) {
        interpretation = "超重";
    } else if (bmi < 30) {
        interpretation = "一级肥胖";
    } else if (bmi < 40) {
        interpretation = "二级肥胖";
    } else {
        interpretation = "三级肥胖";
    }

    // 构建结果Map
    Map<String, Object> data = new HashMap<>();
    data.put("BMI值", String.format("%.2f", bmi)); // 保留两位小数
    data.put("体重范围", interpretation);

    // 可选：根据性别和年龄提供更精准的参考（示例逻辑）
    if (sex != null && age != null) {
        // 可添加针对不同性别、年龄的特殊参考标准
        if (age < 18) {
            data.put("提示", "儿童/青少年BMI标准需结合生长曲线评估，建议咨询医生");
        }
    }

    return new Gson().toJson(data);
}


    @Override
    public String apply(Map<String, Object> args) {
        String sex = (String) args.get("sex");
        Integer age = ((Double) args.get("age")).intValue();
        Integer weight = ((Double) args.get("weight")).intValue();
        Integer height = ((Double) args.get("height")).intValue();
        return search(sex, age, weight, height);
    }

    public static void main(String[] args) {
        BMITool weatherSearchTool = new BMITool("fff");
        String result = weatherSearchTool.search("男", 18, 30, 150);
        System.out.println(result);
    }

}

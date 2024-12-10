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
import java.util.stream.Collectors;


@Setter
public class DishonestPersonSearchTool extends AbstractTool {

    private static final String API_ADDRESS = "https://api.istero.com/resource/laolai";
    private String token = "";

    public DishonestPersonSearchTool() {
        init();
    }

    private void init() {
        name = "dishonest_person_search_tool";
        toolInfo = ToolInfo.builder().name("dishonest_person_search_tool")
                .description("这是一个失信人员查询工具可以通过输入姓名查询同名失信人员信息")
                .args(Lists.newArrayList(
                        ToolArg.builder().name("name").type("string").description("姓名").build()
                        ))
                .build();
        register(this);
    }

    public DishonestPersonSearchTool(String token) {
        this.token = token;
        init();
    }

    private String search(String name) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Authorization", token);
        Map<String, Object> body = new HashMap<>();
        body.put("name", name);
        Gson gson = new Gson();
        String post = ApiInvokeUtil.post(API_ADDRESS, headers, gson.toJson(body), 15, TimeUnit.SECONDS);
        Type type = new TypeToken<Map<String, Object>>(){}.getType();
        Map<String, Object> map = gson.fromJson(post, type);
        if (map == null) {
            return "查询失败";
        }
        int code = ((Double) map.get("code")).intValue();
        if(code != 200) {
            return (String) map.get("message");
        }
        List<Map<String, String>> data  = (List<Map<String, String>>)map.get("data");
        data = data.stream().filter(item -> item.get("name").equals(name))
                .map(info -> {
            info.put("失信人员名字", info.get("name"));
            info.put("失信人员性别", info.get("gender"));
            info.put("失信人员年龄", info.get("age"));
            info.put("失信人员身份证号码/组织信用代码", info.get("cardNum"));
            info.put("区域", info.get("areaName"));
            info.put("案件编号", info.get("caseCode"));
            info.put("法院", info.get("courtName"));
            info.put("责任/判决结果", info.get("duty"));
            info.put("执行结果", info.get("performance"));
            info.put("违法类型", info.get("disruptTypeName"));
            info.put("发布时间", info.get("publishDate"));
            info.remove("name");
            info.remove("gender");
            info.remove("age");
            info.remove("cardNum");
            info.remove("areaName");
            info.remove("caseCode");
            info.remove("courtName");
            info.remove("duty");
            info.remove("performance");
            info.remove("disruptTypeName");
            info.remove("publishDate");
            return info;
        }).collect(Collectors.toList());
        if(data.isEmpty()) {
            return StrUtil.format("没有名叫{}的失信人员", name);
        }
        return gson.toJson(data);
    }


    @Override
    public String apply(Map<String, Object> args) {
        String name = (String) args.get("name");
        return search(name);
    }

    public static void main(String[] args) {
        String token = "xxx";
        DishonestPersonSearchTool weatherSearchTool = new DishonestPersonSearchTool(token);
        String result = weatherSearchTool.search("王五");
        System.out.println(result);
    }

}

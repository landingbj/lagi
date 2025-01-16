package ai.agents.example.tools;

import ai.agent.customer.pojo.ToolInfo;
import ai.agent.customer.tools.AbstractTool;
import ai.utils.ApiInvokeUtil;
import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class GoldTodayTool extends AbstractTool {

    private static final String API_ADDRESS = "https://api.pearktrue.cn/api/goldprice/";

    private Gson gson = new Gson();

    public GoldTodayTool() {
        init();
    }

    private void init() {
        name = "gold_today";
        toolInfo = ToolInfo.builder().name("gold_today")
                .description("这是一个最新的黄金价格的工具,可以获取最新的黄金价格以及各种黄金的详细信息")
                .args(Lists.newArrayList()).build();
        register(this);
    }

    private String search() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        String post = ApiInvokeUtil.post(API_ADDRESS, headers, "", 15, TimeUnit.SECONDS);
        Gson gson = new Gson();
        Type type = new TypeToken<Map<String, Object>>(){}.getType();
        Map<String, Object> map = gson.fromJson(post, type);
        if (map == null) {
            return "价格查询失败！";
        }
        String formattedString = "价格查询失败!!!";
        if (map.get("msg") != null && map.get("msg").equals("获取成功")) {
            List<Object> data  = new ArrayList<>() ;
            formattedString = StrUtil.format("{\"返回的当前时间\": \"{}\", \"国内黄金价格（单位是元/克）\": \"{}\", \"详细数据:(其中id为序号，dir为黄金目录，title为黄金名，changepercent为涨跌幅，maxprice为最高价，minprice为最低价，openingprice为开盘价，lastclosingprice为昨收价，date为日期，status code为状态码，msg为状态描述。)\": \"{}\"",
                    map.get("time"),
                    map.get("price"),
                    map.get("data"));
        }
        return formattedString;
    }
    @Override
    public String apply(Map<String, Object> stringObjectMap) {
        return search();
    }

    public static void main(String[] args) {
        GoldTodayTool tool = new GoldTodayTool();
        String result = tool.search();
        System.out.println(result);
    }


}

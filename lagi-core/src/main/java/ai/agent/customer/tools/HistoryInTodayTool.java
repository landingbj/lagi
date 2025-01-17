package ai.agent.customer.tools;

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
public class HistoryInTodayTool extends AbstractTool {
    // doc https://api.aa1.cn/doc/baike.html
    private static final String API_ADDRESS = "https://zj.v.api.aa1.cn/api/bk/?num=5&type=json";
    private String token = "";

    public HistoryInTodayTool() {
        init();
    }

    private void init() {
        name = "history_in_today_tool";
        toolInfo = ToolInfo.builder().name("history_in_today_tool")
                .description("这是一个查询历史上今天的发生的事件的工具可以查询历史上今天发生了什么事件")
                .args(Lists.newArrayList())
                .build();
        register(this);
    }

    public HistoryInTodayTool(String token) {
        this.token = token;
        init();
    }

    private String search() {
        Map<String, String> params = new HashMap<>();
        params.put("type", "json");
        params.put("num", "1");
        Gson gson = new Gson();
        String post = ApiInvokeUtil.get(API_ADDRESS, params, null, 15, TimeUnit.SECONDS);
        Type type = new TypeToken<Map<String, Object>>(){}.getType();
        Map<String, Object> map = gson.fromJson(post, type);
        if (map == null) {
            return "查询失败";
        }
        if(((Double)map.get("code")).intValue() != 200) {
            return "抱歉历史上今天的事件失败";
        }
        List<String> data  = (List<String>)map.get("content");
        return data.toString();
    }


    @Override
    public String apply(Map<String, Object> args) {
        return search();
    }

    public static void main(String[] args) {
        HistoryInTodayTool highSpeedTicketTool = new HistoryInTodayTool();
        System.out.println(highSpeedTicketTool.search());
    }

}

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
//    private static final String API_ADDRESS = "https://zj.v.api.aa1.cn/api/bk/?num=5&type=json";

//   doc https://www.free-api.com/doc/72
    private static final String API_ADDRESS = "https://api.asilu.com/today";
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
        Gson gson = new Gson();
        String post = ApiInvokeUtil.get(API_ADDRESS, params, null, 15, TimeUnit.SECONDS);
        Type type = new TypeToken<Map<String, Object>>(){}.getType();
        Map<String, Object> map = gson.fromJson(post, type);
        if (map == null) {
            return "查询失败";
        }
        if(((Double)map.get("code")).intValue() != 200) {
            return "历史上今天没有发生什么大事，是平安太平的一天。";
        }
        List<Map<String, Object>> data  = (List<Map<String, Object>>)map.get("data");
        StringBuilder sb = new StringBuilder();
        data.forEach(item -> {
            sb.append(item.get("year")).append("年:").append(item.get("title")).append("\n");
        });
        return sb.toString();
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

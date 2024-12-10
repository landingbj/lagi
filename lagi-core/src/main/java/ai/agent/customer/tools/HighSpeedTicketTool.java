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
public class HighSpeedTicketTool extends AbstractTool {
    // doc https://api.aa1.cn/doc/pear_highSpeedTicket.html
    private static final String API_ADDRESS = "https://tools.mgtv100.com/external/v1/pear/highSpeedTicket";
    private String token = "";

    public HighSpeedTicketTool() {
        init();
    }

    private void init() {
        name = "high_speed_ticket_tool";
        toolInfo = ToolInfo.builder().name("high_speed_ticket_tool")
                .description("这是一个高铁票查询工具可以通过输入行程的起点、终点出发日期查询高铁票信息")
                .args(Lists.newArrayList(
                        ToolArg.builder().name("from").type("string").description("行程起始地点").build(),
                        ToolArg.builder().name("to").type("string").description("行程终点").build(),
                        ToolArg.builder().name("time").type("string").description("出发日期,格式为 yyyy-mm-dd").build()
                        ))
                .build();
        register(this);
    }

    public HighSpeedTicketTool(String token) {
        this.token = token;
        init();
    }

    private String search(String from, String to, String time) {
        Map<String, Object> body = new HashMap<>();
        if(from != null) {
            body.put("from", from);
        }
        if(to != null) {
            body.put("to", to);
        }
        if(time != null) {
            body.put("time", time);
        }
        Gson gson = new Gson();
        String post = ApiInvokeUtil.post(API_ADDRESS, null, gson.toJson(body), 15, TimeUnit.SECONDS);
        Type type = new TypeToken<Map<String, Object>>(){}.getType();
        Map<String, Object> map = gson.fromJson(post, type);
        if (map == null) {
            return "查询失败";
        }
        if(((Double)map.get("code")).intValue() != 200) {
            return "抱歉查询高铁信息失败";
        }
        List<Map<String, Object>> data  = (List<Map<String, Object>>)map.get("data");
        StringBuffer sb = new StringBuffer();
        int count = 0;
        for (Map<String, Object> item : data) {
            if (count > 5) {
                break;
            }
            List<Map<String, Object>> tickets = (List<Map<String, Object>>) item.get("ticket_info");
            StringBuffer tb = new StringBuffer();
            tickets.forEach(ticket -> {
                int seatinventory = ((Double) ticket.get("seatinventory")).intValue();
                if (seatinventory <= 0) {
                    return;
                }
                tb.append(StrUtil.format("\t坐次:{} 剩余车牌数量:{} 车票价格:{} ", ticket.get("seatname"), ticket.get("seatinventory"), ticket.get("seatprice")));
            });
            if (tb.toString().isEmpty()) break;
            String msg = StrUtil.format("列车编号:{} 起始车站:{} 结束车站:{} 出发时间: {} 到达时间： {} 路程耗时: {} 车票信息: {}", item.get("trainumber"), item.get("departstation"), item.get("arrivestation"), item.get("departtime"), item.get("arrivetime"), item.get("runtime"), tb.toString());
            sb.append(msg).append("\n");
            count++;
        };
        return sb.toString();
    }


    @Override
    public String apply(Map<String, Object> args) {
        return search((String) args.get("from"), (String) args.get("to"), (String) args.get("time"));
    }

    public static void main(String[] args) {
        HighSpeedTicketTool highSpeedTicketTool = new HighSpeedTicketTool();
//        System.out.println(highSpeedTicketTool.search("武汉", "深圳", "2024-12-10"));
    }

}

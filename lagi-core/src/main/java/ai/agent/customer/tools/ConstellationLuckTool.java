package ai.agent.customer.tools;

import ai.agent.customer.pojo.ToolArg;
import ai.agent.customer.pojo.ToolInfo;
import ai.utils.ApiInvokeUtil;
import com.google.common.collect.Lists;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class ConstellationLuckTool extends AbstractTool {

    private static final String API_ADDRESS = "https://api.52vmy.cn/api/wl/s/xingzuo?type=JSON";

    public ConstellationLuckTool() {
        init();
    }

    private void init() {
        name = "constellation_luck";
        toolInfo = ToolInfo.builder().name("constellation_luck")
                .description("这是一个根据星座查询今日运势的工具，您只需告诉我您的星座，我就可以告诉你今日的星座运势。")
                .args(Lists.newArrayList(
                        ToolArg.builder().name("msg").type("string").description("星座").build()
                )).build();
        register(this);
    }

    private String search(String msg) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        String post = ApiInvokeUtil.post(API_ADDRESS+"&msg="+msg, headers, "", 15, TimeUnit.SECONDS);

        if (post == null) {
            return "查询失败";
        }
        return post.toString();
    }


    @Override
    public String apply(Map<String, Object> args) {
        return search(args.get("msg").toString());
    }

    public static void main(String[] args) {
        ConstellationLuckTool tool = new ConstellationLuckTool();
        String result = tool.search("双鱼");
        System.out.println(result);
    }

}

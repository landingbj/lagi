package ai.agent.customer.tools;

import ai.agent.customer.pojo.ToolArg;
import ai.agent.customer.pojo.ToolInfo;
import ai.utils.ApiInvokeUtil;
import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class IPAddressLookupTool extends AbstractTool {

    private static final String API_ADDRESS = "https://api.52vmy.cn/api/query/itad?type=json";

    public IPAddressLookupTool() {
        init();
    }

    private void init() {
        name = "ip_address_lookup_agent";
        toolInfo = ToolInfo.builder().name("ip_address_lookup_agent")
                .description("这是一个查询IP地址的归属地的工具，您只需告诉我ip地址，我就可以告诉你所在的归属地。")
                .args(Lists.newArrayList(
                        ToolArg.builder().name("ip").type("string").description("ip地址").build()
                )).build();
        register(this);
    }

    private String search(String ip) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        String post = ApiInvokeUtil.post(API_ADDRESS+"&ip="+ip, headers, "", 15, TimeUnit.SECONDS);
        Gson gson = new Gson();
        Type type = new TypeToken<Map<String, Object>>(){}.getType();
        Map<String, Object> map = gson.fromJson(post, type);
        if (map == null) {
            return "查询失败";
        }
        Map<String, Object> data  = (Map<String, Object>)map.get("data");
//        Map<String, Object> data = gson.fromJson(dataStr, type);
        return StrUtil.format("{\"ip地址\": \"{}\",\"归属地\": \"{}\"}",data.get("ip"),data.get("address"));
    }


    @Override
    public String apply(Map<String, Object> args) {
        return search(args.get("ip").toString());
    }

    public static void main(String[] args) {
        IPAddressLookupTool tool = new IPAddressLookupTool();
        String result = tool.search("192.168.10.1");
        System.out.println(result);
    }

}

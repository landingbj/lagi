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

public class BaiduSearchPicturesTool extends AbstractTool {

    private static final String API_ADDRESS = "https://zj.v.api.aa1.cn/api/so-baidu-img/?page=1";

    public BaiduSearchPicturesTool() {
        init();
    }

    private void init() {
        name = "baidu_search_pictures";
        toolInfo = ToolInfo.builder().name("baidu_search_pictures")
                .description("这是一个百度搜图工具，可以根据您的关键词帮助您在网上搜索一些图片")
                .args(Lists.newArrayList(ToolArg.builder().name("msg").type("string").description("关键词").build())).build();
        register(this);
    }

    private String search(String msg) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        String post = ApiInvokeUtil.post(API_ADDRESS+"&msg="+msg, headers, "", 15, TimeUnit.SECONDS);
        Gson gson = new Gson();
        Type type = new TypeToken<Map<String, Object>>(){}.getType();
        Map<String, Object> map = gson.fromJson(post, type);
        if (map == null) {
            return "查询失败";
        }
        Object data  = (Object)map.get("data");
//        Map<String, Object> data = gson.fromJson(dataStr, type);
        return StrUtil.format("{\"相关内容\": \"{}\"}",data);
    }


    @Override
    public String apply(Map<String, Object> args) {
        return search(args.get("msg").toString());
    }

    public static void main(String[] args) {
        BaiduSearchPicturesTool tool = new BaiduSearchPicturesTool();
        String result = tool.search("王腾有大帝之姿");
        System.out.println(result);
    }

}

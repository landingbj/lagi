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

public class SogouSearchPicturesTool extends AbstractTool {

    private static final String API_ADDRESS = "https://api.52vmy.cn/api/img/sogo";

    public SogouSearchPicturesTool() {
        init();
    }

    private void init() {
        name = "sogou_search_pictures";
        toolInfo = ToolInfo.builder().name("sogou_search_pictures")
                .description("这是一个搜狗搜图工具，可以根据您的关键词帮助您在网上搜索一张图片")
                .args(Lists.newArrayList(ToolArg.builder().name("msg").type("string").description("关键词").build())).build();
        register(this);
    }

    private String search(String msg) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        String post = ApiInvokeUtil.post(API_ADDRESS+"?msg="+msg, headers, "", 15, TimeUnit.SECONDS);
        Gson gson = new Gson();
        Type type = new TypeToken<Map<String, Object>>(){}.getType();
        Map<String, Object> map = gson.fromJson(post, type);
        if (map == null) {
            return "查询失败";
        }
        Map<String, Object> data  = (Map<String, Object>)map.get("data");
//        Map<String, Object> data = gson.fromJson(dataStr, type);
        return StrUtil.format("{\"图片介绍\": \"{}\",\"图片地址\": \"{}\"}",data.get("title"),data.get("url"));
    }


    @Override
    public String apply(Map<String, Object> args) {
        return search(args.get("msg").toString());
    }

    public static void main(String[] args) {
        SogouSearchPicturesTool tool = new SogouSearchPicturesTool();
        String result = tool.search("王腾有大帝之姿");
        System.out.println(result);
    }

}

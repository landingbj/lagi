package ai.agent.customer.tools;

import ai.agent.customer.pojo.ToolArg;
import ai.agent.customer.pojo.ToolInfo;
import cn.hutool.core.util.StrUtil;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class ToolUtils {
    public static String genToolPrompt(List<ToolInfo> toolInfos) {
        Gson gson = new Gson();
        List<String> tools = new ArrayList<>();
        for (int i = 0; i < toolInfos.size(); i++) {
            ToolInfo toolInfo = toolInfos.get(i);
            List<String> args = new ArrayList<>();
            for (ToolArg arg : toolInfo.getArgs()) {
                args.add(gson.toJson(arg));
            }
            String argsJson = gson.toJson(args);
            String format = StrUtil.format("{}. {}: {}, args: {}", i + 1, toolInfo.getName(), toolInfo.getDescription(), argsJson);
            tools.add(format);
        }
        return String.join("\n", tools);
    }
}

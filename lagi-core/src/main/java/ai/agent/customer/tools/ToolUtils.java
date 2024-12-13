package ai.agent.customer.tools;

import ai.agent.customer.pojo.ToolArg;
import ai.agent.customer.pojo.ToolInfo;
import cn.hutool.core.util.StrUtil;

import java.util.ArrayList;
import java.util.List;

public class ToolUtils {
    public static String genToolPrompt(List<ToolInfo> toolInfos) {
        List<String> tools = new ArrayList<>();
        for (int i = 0; i < toolInfos.size(); i++) {
            ToolInfo toolInfo = toolInfos.get(i);
            List<String> args = new ArrayList<>();
            for (ToolArg arg : toolInfo.getArgs()) {
                String format = StrUtil.format("{  \"name\": \"{}\",  \"description\": \"{}\",   \"type\": \"{}\"}", arg.getName(), arg.getDescription(), arg.getType());
                args.add(format);
            }
            String argsJson = "[" + String.join(",", args) + "]";
            String format = StrUtil.format("{}. {}: {}, args:", i + 1, toolInfo.getName(), toolInfo.getDescription());
            tools.add(format + argsJson);
        }
        return String.join("\n", tools);
    }
}

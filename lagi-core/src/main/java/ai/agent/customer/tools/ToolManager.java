package ai.agent.customer.tools;

import lombok.Getter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ToolManager {
    private final Map<String, AbstractTool> tools = new ConcurrentHashMap<>();
    @Getter
    private static ToolManager instance = new ToolManager();

    public void registerTool(AbstractTool tool) {
        tools.put(tool.getName(), tool);
    }

    public AbstractTool getTool(String name) {
        return tools.get(name);
    }

}

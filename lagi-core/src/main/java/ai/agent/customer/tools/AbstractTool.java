package ai.agent.customer.tools;

import ai.agent.customer.pojo.ToolInfo;
import lombok.Getter;

import java.util.Map;
import java.util.function.Function;

@Getter
public abstract class AbstractTool implements Function<Map<String, Object>, String> {
    protected String name;

    protected ToolInfo toolInfo;
    protected void register(AbstractTool abstractTool)
    {
        ToolManager.getInstance().registerTool(abstractTool);
    }
}

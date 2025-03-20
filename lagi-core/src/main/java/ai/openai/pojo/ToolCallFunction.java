package ai.openai.pojo;

import lombok.Data;

@Data
public class ToolCallFunction {
    private String name;
    private String arguments;
}

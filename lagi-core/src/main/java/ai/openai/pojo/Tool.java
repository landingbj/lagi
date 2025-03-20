package ai.openai.pojo;

import lombok.Data;

@Data
public class Tool {
    private String type;
    private Function function;
}

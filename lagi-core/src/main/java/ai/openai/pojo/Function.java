package ai.openai.pojo;

import lombok.Data;

@Data
public class Function {
    private String name;
    private String description;
    private Boolean strict = Boolean.TRUE;
    private Parameters parameters;
}

package ai.openai.pojo;

import lombok.Data;

import java.util.List;
import java.util.Map;
@Data
public class Parameters {
    private String type;
    private Map<String, Property> properties;
    private List<String> required;
    private Boolean additionalProperties = false;
}

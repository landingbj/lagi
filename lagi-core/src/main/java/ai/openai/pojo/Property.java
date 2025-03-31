package ai.openai.pojo;

import lombok.Data;

import java.util.List;
@Data
public class Property {
    private String type;
    private String description;
    private List<String> enums;
}

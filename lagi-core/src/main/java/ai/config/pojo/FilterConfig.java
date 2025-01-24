package ai.config.pojo;

import lombok.Data;

import java.util.List;

@Data
public class FilterConfig {
    private String name;
    private List<FilterRule> groups;
    private String rules;
}

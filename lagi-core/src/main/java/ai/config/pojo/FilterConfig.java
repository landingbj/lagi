package ai.config.pojo;

import ai.common.pojo.WordRules;
import lombok.Data;

import java.util.List;

@Data
public class FilterConfig {
    private WordRules sensitive;
    private List<String> stopping;
    private List<String> priority;
    private List<String> retain;
}

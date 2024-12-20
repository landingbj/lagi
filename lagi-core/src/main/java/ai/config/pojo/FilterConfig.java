package ai.config.pojo;

import ai.common.pojo.WordRules;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class FilterConfig {
    private WordRules sensitive;
    private List<String> stopping;
    private List<String> priority;
    private List<String> retain;
    @JsonProperty("continue")
    private List<String> continueWords;
}

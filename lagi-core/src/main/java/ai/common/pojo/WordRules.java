package ai.common.pojo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
@AllArgsConstructor
@ToString
@Builder
public class WordRules {
    private String mask;
    private Integer level;
    private List<WordRule> rules;

    public WordRules() {
        mask = "...";
        level = 3;
    }
}

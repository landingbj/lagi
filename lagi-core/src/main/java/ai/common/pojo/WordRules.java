package ai.common.pojo;

import lombok.*;

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

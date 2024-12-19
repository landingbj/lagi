package ai.common.pojo;

import lombok.*;

@Data
@AllArgsConstructor
@ToString
@Builder
@NoArgsConstructor
public class WordRule {

    private String rule;
    /**
     * 0 Nothing to do, 1 remove all, 2 mask  3 erase
     */
    private Integer level;
    private String mask;
}

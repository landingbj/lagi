package ai.vector.loader.pojo;

import lombok.*;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class SplitConfig {
    private Integer chunkSizeForText;
    private Integer chunkSizeForMixUp;
    private Integer chunkSizeForTable;
    private String category;
    private Map<String, Object> extra;
}

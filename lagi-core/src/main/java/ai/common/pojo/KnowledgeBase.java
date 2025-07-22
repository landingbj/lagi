package ai.common.pojo;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class KnowledgeBase {
    private Long id;
    private String userId;
    private String name;
    private String description;
    private String region;
    private String category;
    private Long settingsId;
    private boolean isPublic;
    private Boolean enableGraph;
    private Boolean enableText2qa;
    private Boolean enableFulltext;
    private Integer wenbenChunkSize;
    private Integer biaogeChunkSize;
    private Integer tuwenChunkSize;
    private Integer similarityTopK;
    private Double similarityCutoff;

    private Long createTime;
    private Long updateTime;

    private Integer fileCount;
}

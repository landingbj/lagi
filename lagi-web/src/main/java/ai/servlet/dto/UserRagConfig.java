package ai.servlet.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import java.sql.Timestamp;

@Data
@EqualsAndHashCode
public class UserRagConfig {
    private Long id;
    private String userId;
    private Long knowledgeBaseId;
    private boolean enableFulltext;
    private boolean enableGraph;
    private boolean enableText2qa;
    private int wenbenChunkSize;
    private int biaogeChunkSize;
    private int tuwenChunkSize;
    private int similarityTopK;
    private double similarityCutoff;


}

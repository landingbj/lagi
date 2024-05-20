package ai.common.pojo;

import ai.vector.VectorStoreConstant;
import lombok.Data;
import lombok.ToString;

@ToString
@Data
public class VectorStoreConfig {
    private String name;
    private String driver;
    private String type;
    private String defaultCategory;
    private String url;
    private String metric = VectorStoreConstant.VECTOR_METRIC_COSINE;
    private String apiKey;
    private String indexName;
    private String environment;
    private String projectName;

    private Integer similarityTopK;
    private Double similarityCutoff;
    private Integer parentDepth;
    private Integer childDepth;


}

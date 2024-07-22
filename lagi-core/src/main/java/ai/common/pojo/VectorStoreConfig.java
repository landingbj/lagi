package ai.common.pojo;

import ai.vector.VectorStoreConstant;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Data;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE)
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
    @JsonProperty("similarity_top_k")
    private Integer similarityTopK;
    @JsonProperty("similarity_cutoff")
    private Double similarityCutoff;
    @JsonProperty("parent_depth")
    private Integer parentDepth;
    @JsonProperty("child_depth")
    private Integer childDepth;
    @JsonProperty("token")
    private String token;

    VectorStoreConfig() {
        similarityTopK = 1;
        similarityCutoff = 0.5;
        parentDepth = 0;
        childDepth = 0;
    }

}

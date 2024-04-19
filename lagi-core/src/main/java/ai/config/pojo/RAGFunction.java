package ai.config.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Data
public class RAGFunction {
    private String name;
    @JsonProperty("similarity_top_k")
    private Integer similarityTopK;
    @JsonProperty("similarity_cutoff")
    private Double similarityCutoff;
    @JsonProperty("parent_depth")
    private Integer parentDepth;
    @JsonProperty("child_depth")
    private Integer childDepth;

    RAGFunction() {
        similarityTopK = 1;
        similarityCutoff = 0.5;
        parentDepth = 0;
        childDepth = 0;
    }
}

package ai.config.pojo;

import ai.common.pojo.Medusa;
import ai.common.pojo.VectorStoreConfig;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class StoreConfig {
    @JsonProperty("vector")
    private List<VectorStoreConfig> vectors;
    private List<OSSConfig> oss;
    private RAGFunction rag;
    private List<BigdataConfig> bigdata;
    private Medusa medusa;
}

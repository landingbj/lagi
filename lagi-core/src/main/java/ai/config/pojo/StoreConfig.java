package ai.config.pojo;

import ai.common.pojo.Backend;
import ai.common.pojo.Medusa;
import ai.common.pojo.VectorStoreConfig;
import lombok.Data;

import java.util.List;

@Data
public class StoreConfig {
    private List<VectorStoreConfig> vectors;
    private List<OSSConfig> oss;
    private List<Backend> rag;
    private Medusa medusa;
}

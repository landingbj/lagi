package ai.medusa.pojo;

import ai.common.pojo.IndexSearchData;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

@Data
@Builder
public class PooledPrompt implements Serializable {
    private PromptInput promptInput;
    private Integer status;
    private List<IndexSearchData> indexSearchData;
}

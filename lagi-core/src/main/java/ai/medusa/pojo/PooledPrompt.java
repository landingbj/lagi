package ai.medusa.pojo;

import ai.common.pojo.IndexSearchData;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@ToString
public class PooledPrompt implements Serializable {
    private PromptInput promptInput;
    private Integer status;
    private List<IndexSearchData> indexSearchData;
}

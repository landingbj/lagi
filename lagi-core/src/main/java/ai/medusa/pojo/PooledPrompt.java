package ai.medusa.pojo;

import ai.common.pojo.IndexSearchData;
import lombok.*;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class PooledPrompt implements Serializable {
    private PromptInput promptInput;
    private Integer status;
    private List<IndexSearchData> indexSearchData;
    @Builder.Default
    private Boolean needSplitBoundary = true;
}

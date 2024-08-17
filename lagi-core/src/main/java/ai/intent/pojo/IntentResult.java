package ai.intent.pojo;

import ai.common.pojo.IndexSearchData;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@Data
public class IntentResult {
    /**
     * text image video
     */
    private String type;
    /**
     * continue 、 completion
     */
    private String status;

    private Integer continuedIndex;

    private List<IndexSearchData> indexSearchDataList;
}

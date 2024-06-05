package ai.intent.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

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
}

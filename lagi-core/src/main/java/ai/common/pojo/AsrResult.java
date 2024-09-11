package ai.common.pojo;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
public class AsrResult {
    private String task_id;
    private String result;
    private Integer status;
    private String message;
}

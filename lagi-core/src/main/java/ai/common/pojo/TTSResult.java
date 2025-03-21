package ai.common.pojo;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class TTSResult {
    private String task_id;
    private String result;
    private Integer status;
    private String message;
}

package ai.common.pojo;

import lombok.Data;

@Data
public class TTSResult {
    private String task_id;
    private String result;
    private Integer status;
    private String message;
}

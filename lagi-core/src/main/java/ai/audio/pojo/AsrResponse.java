package ai.audio.pojo;

import ai.worker.pojo.WorkerProcessResult;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AsrResponse {
    private Integer code;
    private String msg;
}

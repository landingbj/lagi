package ai.audio.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AsrResponse {
    private Integer code;
    private String msg;
}

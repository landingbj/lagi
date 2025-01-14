package ai.common.pojo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Response {
    private String status;
    private String data;
    private String msg;
}

package ai.common.pojo;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class BackendMatch {
    private String backend;
    private Boolean enable;
    private Boolean stream;
    private Integer priority;
}

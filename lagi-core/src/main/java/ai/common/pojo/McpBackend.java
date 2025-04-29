package ai.common.pojo;

import lombok.*;

import java.util.Map;

@Data
@NoArgsConstructor
@ToString
@Builder
public class McpBackend {
    private String name;
    private String url;
    private String key;
    private Map<String, Object> headers;
    private Integer priority;
    private Boolean enable;
    private String driver;

    public McpBackend(String name,
                      String url,
                      String key,
                      Map<String, Object> headers,
                      Integer priority,
                      Boolean enable,
                      String driver) {
        this.name = name;
        this.url = url;
        this.key = key;
        this.headers = headers;
        this.priority = priority;
        this.enable = enable;
        this.driver = driver;
        if (this.driver == null) {
            this.driver = "ai.mcps.CommonSseMcpClient";
        }
    }
}

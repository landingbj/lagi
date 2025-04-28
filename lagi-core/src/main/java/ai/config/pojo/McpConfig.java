package ai.config.pojo;

import ai.common.pojo.McpBackend;
import lombok.Data;

import java.util.List;

@Data
public class McpConfig {
    private List<McpBackend> server;
}

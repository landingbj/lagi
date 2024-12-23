package ai.servlet.dto;

import ai.config.pojo.AgentConfig;
import lombok.Data;

import java.util.List;

@Data
public class LagiAgentResponse {
    private String status;
    private AgentConfig data;
}

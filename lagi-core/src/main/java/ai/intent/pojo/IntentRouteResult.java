package ai.intent.pojo;

import ai.config.pojo.AgentConfig;
import lombok.Data;

import java.util.List;

@Data
public class IntentRouteResult {
    private String modal;
    private String status;
    private Integer continuedIndex;
    private List<AgentConfig> agents;
}

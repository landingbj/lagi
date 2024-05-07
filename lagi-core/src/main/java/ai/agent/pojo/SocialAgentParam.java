package ai.agent.pojo;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
public class SocialAgentParam extends AgentParam {
    private String username;
    private String robotFlag;
    private String timerFlag;
    private String repeaterFlag;
    private String guideFlag;
}

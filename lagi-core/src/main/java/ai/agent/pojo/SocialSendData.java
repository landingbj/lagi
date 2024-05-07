package ai.agent.pojo;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class SocialSendData extends AgentData {
    private String channelUser;
    private String text;
}

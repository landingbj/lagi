package ai.agent.pojo;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class SocialReceiveData extends AgentData {
    private String status;
    private String data;
}

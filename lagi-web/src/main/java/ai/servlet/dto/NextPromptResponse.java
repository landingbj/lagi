package ai.servlet.dto;

import lombok.Data;

@Data
public class NextPromptResponse {
    private String status;
    private String nextAction;
    private String appId;
    private String username;
    private Integer channelId;
}

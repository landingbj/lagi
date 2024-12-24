package ai.worker.pojo;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class AgentIntentScore {
    private String agentId;
    private String agentName;
    private String keyword;
    private String question;
    private Double score;
}
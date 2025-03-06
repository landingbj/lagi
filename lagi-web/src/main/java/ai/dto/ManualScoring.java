package ai.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class ManualScoring {
    private String question;
    private Integer agentId;
    private Double score;
}

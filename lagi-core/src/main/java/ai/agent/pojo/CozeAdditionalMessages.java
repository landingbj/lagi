package ai.agent.pojo;

import lombok.*;


@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
@ToString
public class CozeAdditionalMessages {
    private String role;
    private String content;
    private String content_type;
}

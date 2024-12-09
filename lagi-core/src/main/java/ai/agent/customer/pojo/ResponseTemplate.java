package ai.agent.customer.pojo;

import lombok.*;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ResponseTemplate {
    private Action action;
    private Thoughts thoughts;
}

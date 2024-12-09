package ai.agent.customer.pojo;

import lombok.*;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ArgDescription {
    private String name;
    private String type;
    private String description;
}

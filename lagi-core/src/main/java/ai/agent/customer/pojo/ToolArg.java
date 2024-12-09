package ai.agent.customer.pojo;

import lombok.*;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ToolArg {
    private String name;
    private String description;
    private String type;
}

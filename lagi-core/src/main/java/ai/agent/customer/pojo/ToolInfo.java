package ai.agent.customer.pojo;

import lombok.*;

import java.util.List;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ToolInfo {
    private String name;
    private String description;
    private List<ToolArg> args;
}

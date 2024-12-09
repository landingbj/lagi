package ai.agent.customer.pojo;

import lombok.*;

import java.util.Map;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Action {
    private String name;
    private Map<String, Object> args;
}

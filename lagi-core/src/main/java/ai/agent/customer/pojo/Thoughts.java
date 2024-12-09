package ai.agent.customer.pojo;

import lombok.*;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Thoughts {
    private String text;
    private String plain;
    private String criticism;
    private String speak;
    private String reasoning;
}

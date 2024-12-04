package ai.agent.chat.tencent.pojo;

import lombok.*;

import java.util.List;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Steps {
    private String role;

    private String content;

    private List<ToolCalls> toolCalls;

    private Usage usage;

    private Integer time_cost;
}

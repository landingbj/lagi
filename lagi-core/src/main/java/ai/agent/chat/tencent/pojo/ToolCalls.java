package ai.agent.chat.tencent.pojo;

import lombok.*;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ToolCalls {
    private String id;

    private String type;

    private Function function;
}

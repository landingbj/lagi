package ai.agent.chat.tencent.pojo;

import lombok.*;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Function {
    private String name;

    private String desc;

    private String type;

    private String arguments;
}

package ai.agent.chat.zhipu.pojo;

import lombok.*;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class KeyValuePairs {
    private String id;
    private String type;
    private String name;
    private String value;
}

package ai.agent.chat.zhipu.pojo;

import lombok.*;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DataJson {
    private String msg;
    private ExtraInput extra_input;
    private Usage usage;
}

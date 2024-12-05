package ai.agent.chat.zhipu.pojo;

import lombok.*;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ExtraInput {
    private String request_id;
    private String node_id;
    private String push_type;
    private Object node_data;
    private Object block_data;
}

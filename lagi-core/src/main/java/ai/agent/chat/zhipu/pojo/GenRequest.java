package ai.agent.chat.zhipu.pojo;

import lombok.*;

import java.util.List;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GenRequest {
    private String app_id;

    private String conversation_id;

    private List<KeyValuePairs> key_value_pairs;
}

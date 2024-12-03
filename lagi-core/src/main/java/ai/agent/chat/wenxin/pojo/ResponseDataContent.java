package ai.agent.chat.wenxin.pojo;

import lombok.*;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ResponseDataContent {
    private String dataType;
    private String data;
}

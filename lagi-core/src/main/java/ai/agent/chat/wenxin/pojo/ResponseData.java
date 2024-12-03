package ai.agent.chat.wenxin.pojo;

import lombok.*;

import java.util.List;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ResponseData {
    private String threadId;
    private String msgId;
    private List<ResponseDataContent> content;
}

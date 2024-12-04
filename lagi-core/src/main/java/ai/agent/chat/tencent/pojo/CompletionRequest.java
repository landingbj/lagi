package ai.agent.chat.tencent.pojo;

import lombok.*;

import java.util.List;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CompletionRequest {
    private String assistant_id;

    private String userId;

    private boolean stream;

    private List<Messages> messages;

}

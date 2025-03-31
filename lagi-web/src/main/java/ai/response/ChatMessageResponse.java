package ai.response;

import ai.openai.pojo.ChatMessage;
import lombok.*;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ChatMessageResponse extends ChatMessage {
    private List<String> contextChunkIds;
}

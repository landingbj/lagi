package ai.openai.pojo;

import lombok.*;

@Data
@EqualsAndHashCode
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VlChatContent {
    private String type;
    private String text;
    private VlChatContentImage image_url;
}

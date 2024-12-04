package ai.agent.chat.tencent.pojo;

import lombok.*;

import java.util.List;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Message {
    private String role;

    private String content;

    private List<Steps> steps;
}

package ai.agent.chat.tencent.pojo;

import lombok.*;

import java.util.List;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Messages
{
    private String role;

    private List<Content> content;
}

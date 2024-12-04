package ai.agent.chat.tencent.pojo;

import lombok.*;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Content
{
    private String type;

    private String text;
}
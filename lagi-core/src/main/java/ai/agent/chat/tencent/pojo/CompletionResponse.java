package ai.agent.chat.tencent.pojo;


import lombok.*;

import java.util.List;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CompletionResponse {
    private String id;

    private String created;

    private List<Choices> choices;

    private String assistant_id;

    private Usage usage;
}

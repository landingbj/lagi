package ai.agent.pojo;

import lombok.*;

@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
@ToString
public class CozeChatResponse<T> {
    private T data;
    private Integer code;
    private String msg;
}

package ai.agent.chat.zhipu.pojo;

import lombok.*;


@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Response {
    private ResponseData data;
    private Integer code;
    private String message;
    private Long timestamp;
}

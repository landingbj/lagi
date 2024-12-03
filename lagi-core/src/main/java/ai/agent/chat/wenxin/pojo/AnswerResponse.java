package ai.agent.chat.wenxin.pojo;


import lombok.*;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AnswerResponse {
    private Integer status;
    private String message;
    private String logid;
    private ResponseData data;
}

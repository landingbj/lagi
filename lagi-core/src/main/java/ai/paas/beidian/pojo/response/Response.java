package ai.paas.beidian.pojo.response;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Response <T>{
    private Integer code;
    private String msg;
    private String message;
    private String id;
    private T data;
}

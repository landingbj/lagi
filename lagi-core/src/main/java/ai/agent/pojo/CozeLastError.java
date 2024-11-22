package ai.agent.pojo;

import lombok.*;

@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
@ToString
public class CozeLastError {
    private int code;
    private String msg;
}

package ai.agent.carbus.pojo;

import lombok.*;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ApiResponse<T> {
    private int code;
    private String msg;
    private T data;
    private boolean ok;
}

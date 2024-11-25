package ai.webSocket.pojo;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
@Data
public class ReservationRequest {
    private String msg;
    private String devId;
    private String skillCode;
    private boolean llm;
    private Entity entity;
}

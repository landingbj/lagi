package ai.webSocket.pojo;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
@Data
public class Entity {
    private String skillCode;
    private String start_time;
    private String date;
}

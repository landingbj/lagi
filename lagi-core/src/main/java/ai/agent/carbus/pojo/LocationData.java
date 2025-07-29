package ai.agent.carbus.pojo;

import lombok.*;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class LocationData {
    private List<LocationTip> tips;
    private String status;
    private String info;
    private String infocode;
    private String count;
}

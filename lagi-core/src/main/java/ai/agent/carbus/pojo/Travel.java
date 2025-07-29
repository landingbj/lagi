package ai.agent.carbus.pojo;

import lombok.*;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Travel {
    private String witchDay;
    private List<AttractionInfo> morning;
    private List<AttractionInfo> afternoon;
    private List<AttractionInfo> evening;
    private List<AttractionInfo> night;
}

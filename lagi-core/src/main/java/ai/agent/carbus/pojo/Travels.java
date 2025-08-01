package ai.agent.carbus.pojo;

import lombok.*;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Travels {
    private String title;
    private String summary;
    private List<Travel> travels;
}

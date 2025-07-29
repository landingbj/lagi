package ai.agent.carbus.pojo;

import lombok.*;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class AttractionInfo {
    private String name;
    private Double longitude;
    private Double latitude;
    private String imageUrl;
    private String description;
}

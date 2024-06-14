package ai.video.pojo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class OutputVideoProperties {
    private Float duration;
    private Boolean durationAdaption;
    private Integer width;
    private Integer height;
    private String style;
    private Boolean mute;
}

package ai.video.pojo;

import lombok.*;

@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class VideoTackRequest {

    private String videoUrl;
    private String model;
}

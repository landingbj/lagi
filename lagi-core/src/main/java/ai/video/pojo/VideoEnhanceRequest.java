package ai.video.pojo;


import lombok.*;

@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class VideoEnhanceRequest {
    private String model;
    private String videoURL;
    private Boolean async;
    private Integer outPutWidth;
    private Integer outPutHeight;
    private Integer frameRate;
    private String HDRFormat;
    private Integer maxIlluminance;
    private Integer bitrate;
}

package ai.video.pojo;


import lombok.*;

@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class VideoEnhanceRequest {
    public String videoURL;
    public Boolean async;
    public Integer outPutWidth;
    public Integer outPutHeight;
    public Integer frameRate;
    public String HDRFormat;
    public Integer maxIlluminance;
    public Integer bitrate;
}

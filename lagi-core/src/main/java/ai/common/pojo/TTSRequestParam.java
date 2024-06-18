package ai.common.pojo;

import lombok.Data;

@Data
public class TTSRequestParam {
    private String model;
    private String appkey;
    private String text;
    private String token;
    private String format;
    private Integer sample_rate;
    private String voice;
    private Integer volume;
    private Integer speech_rate;
    private Integer pitch_rate;

    private String emotion;

}

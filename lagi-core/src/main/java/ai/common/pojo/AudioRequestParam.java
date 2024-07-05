package ai.common.pojo;

import lombok.Data;

@Data
public class AudioRequestParam {
    private String model;
    private String appkey;
    private String format;
    private Integer sample_rate;
    private String vocabulary_id;
    private String customization_id;
    private Boolean enable_punctuation_prediction;
    private Boolean enable_inverse_text_normalization;
    private Boolean enable_voice_detection;
    private Boolean disfluency;
    private String audio_address;

}

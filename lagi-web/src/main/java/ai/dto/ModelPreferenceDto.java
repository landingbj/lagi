package ai.dto;


import lombok.*;

@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class ModelPreferenceDto {
    private String finger;
    private String userId;
    private String llm;
    private String tts;
    private String asr;
    private String img2Text;
    private String imgGen;
    private String imgEnhance;
    private String img2Video;
    private String text2Video;
    private String videoEnhance;
    private String videoTrack;
}

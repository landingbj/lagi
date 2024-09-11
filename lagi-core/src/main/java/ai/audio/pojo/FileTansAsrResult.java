package ai.audio.pojo;

import lombok.Data;

import java.util.List;

@Data
public class FileTansAsrResult {
    private List<Sentence> Sentences;


    @Data
    public static class Sentence {
        private int EndTime;
        private int SilenceDuration;
        private String SpeakerId;
        private int BeginTime;
        private String Text;
        private int ChannelId;
        private int SpeechRate;
        private double EmotionValue;
    }
}

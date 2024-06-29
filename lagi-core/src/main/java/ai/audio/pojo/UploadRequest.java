package ai.audio.pojo;

import lombok.*;

import java.util.List;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UploadRequest {
    private String model;
    private String speakerId;
    private List<AudioTrain> audioTrains;
}

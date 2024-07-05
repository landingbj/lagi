package ai.audio.pojo;

import lombok.*;

import java.io.File;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AudioTrain {
    private File file;
    private String text;
}

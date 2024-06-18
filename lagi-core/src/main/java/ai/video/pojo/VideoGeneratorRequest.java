package ai.video.pojo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class VideoGeneratorRequest {
    String model;
    OutputVideoProperties outputVideoProperties;
    List<InputFile> inputFileList;
    List<String> intPutText;
}

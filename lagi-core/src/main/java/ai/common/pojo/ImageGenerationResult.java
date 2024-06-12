package ai.common.pojo;

import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ImageGenerationResult {
    private long created;
    private List<ImageGenerationData> data;
    private String dataType;
}

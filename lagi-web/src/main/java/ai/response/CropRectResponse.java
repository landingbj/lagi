package ai.response;

import ai.dto.PageRect;
import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class CropRectResponse {
    private String filename;
    private String filePath;
    private String chunk;
    private List<PageRect> rects;
}

package ai.dto;

import lombok.*;

import java.util.List;


@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CropRequest {
//    private String filename;
    private String filePath;
    private String category;
//    private String chunk;
    private String chunkId;
    private Integer extend;
    private String result;
    private List<PageRect> rects;
}

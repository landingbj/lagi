package ai.response;

import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class ChunkDataResponse {
    private String filename;
    private String filePath;
    private String chunk;
    private List<List<Integer>> rects;
}

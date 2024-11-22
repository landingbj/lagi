package ai.dto;

import lombok.*;

import java.util.List;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChunkFilterRequest {
    private List<String> chunkIds;
    private String category;
    private String result;
}

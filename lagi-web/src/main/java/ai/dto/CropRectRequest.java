package ai.dto;

import lombok.*;

import java.util.List;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CropRectRequest {
    private String category;
    private List<CropRequest> chunkData;
}

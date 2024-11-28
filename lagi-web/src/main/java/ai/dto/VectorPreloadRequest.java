package ai.dto;

import lombok.*;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VectorPreloadRequest {
    private String category;
    private Integer maxLength;
}

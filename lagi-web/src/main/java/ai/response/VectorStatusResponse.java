package ai.response;

import lombok.*;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VectorStatusResponse {
    private Integer status;
    private Integer residue;
}

package ai.image.pojo;

import lombok.*;

@ToString
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SparkGenImgResponse {
    private SparkGenImgHeader header;
    private SparkGenImgPayload payload;
}

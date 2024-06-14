package ai.image.pojo;

import lombok.*;

@ToString
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SparkGenImgRequest {
    private SparkGenImgHeader header;
    private SparkGenImgParam parameter;
    private SparkGenImgPayload payload;
}

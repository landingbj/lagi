package ai.image.pojo;

import lombok.*;

@ToString
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SparkGenImgPayload {
    private SparkMessage message;
    private SparkChoice choices;
}

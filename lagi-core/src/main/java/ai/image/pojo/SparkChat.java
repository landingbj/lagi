package ai.image.pojo;

import lombok.*;

@ToString
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SparkChat {
    private String domain;
    private Integer width;
    private Integer height;
}

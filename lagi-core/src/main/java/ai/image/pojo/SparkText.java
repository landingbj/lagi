package ai.image.pojo;

import lombok.*;

@ToString
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SparkText {
    private String role;
    private String content;
    private Integer index;
}

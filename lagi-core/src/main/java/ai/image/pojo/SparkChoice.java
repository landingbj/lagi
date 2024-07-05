package ai.image.pojo;

import lombok.*;

import java.util.List;

@ToString
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SparkChoice {
    private Integer status;
    private Integer seq;
    private List<SparkText> text;
}

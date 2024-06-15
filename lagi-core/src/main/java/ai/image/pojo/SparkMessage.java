package ai.image.pojo;

import lombok.*;

import java.util.List;

@ToString
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SparkMessage {
    List<SparkText> text;
}

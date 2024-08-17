package ai.common.pojo;

import lombok.*;

@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class QaPair {
    Integer qIndex;
    Integer aIndex;
    String q;
    String a;
}

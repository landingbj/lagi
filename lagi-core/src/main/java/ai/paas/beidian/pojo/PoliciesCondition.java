package ai.paas.beidian.pojo;

import lombok.*;

import java.util.List;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class PoliciesCondition {
    private int repeatType;
    private List<Integer> repeatRange;
    private String repeatTime;
}

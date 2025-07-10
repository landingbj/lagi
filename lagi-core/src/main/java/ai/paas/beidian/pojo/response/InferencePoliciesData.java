package ai.paas.beidian.pojo.response;

import ai.paas.beidian.pojo.PoliciesItem;
import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * 推理策略响应数据
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InferencePoliciesData {

    private int total;
    private List<PoliciesItem> items;


    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Raw {
        private int replicas;
        private Condition condition;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Condition {
        private int repeatType;
        private List<Integer> repeatRange;
        private String repeatTime;
    }
}

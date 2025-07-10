package ai.paas.beidian.pojo;

import ai.paas.beidian.pojo.response.InferencePoliciesData;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 推理策略响应数据
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PoliciesItemRow {
    private int replicas;
    private PoliciesCondition condition;
}

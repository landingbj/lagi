package ai.paas.beidian.pojo;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * 推理策略响应数据
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PoliciesItem {
    private long id;
    private String name;
    private int kind;
    private PoliciesItemRow raw;
    private boolean suspend;
    private long lastExecTime;
    private int lastExecStatus;
    private long createTime;
    private long updateTime;
}

package ai.paas.beidian.pojo;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * 推理服务更新请求参数
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InferenceUpdate {

    /**
     * 目标端口
     */
    private int targetPort;

    /**
     * 协议类型（如 TCP、UDP）
     */
    private String protocol;

    /**
     * 备注信息
     */
    private String remark;
}

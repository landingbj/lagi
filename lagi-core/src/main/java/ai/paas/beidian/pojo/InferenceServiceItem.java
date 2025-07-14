package ai.paas.beidian.pojo;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * 推理服务项数据
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InferenceServiceItem {

    /**
     * 服务ID
     */
    private int serviceId;

    /**
     * 服务名称
     */
    private String serviceName;

    /**
     * 目标端口
     */
    private int targetPort;

    /**
     * 协议类型 (TCP/UDP)
     */
    private String protocol;

    /**
     * 备注信息
     */
    private String remark;
}

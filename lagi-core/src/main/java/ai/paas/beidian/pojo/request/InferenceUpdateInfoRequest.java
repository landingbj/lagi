package ai.paas.beidian.pojo.request;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * 推理服务更新信息请求参数
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InferenceUpdateInfoRequest {

    /**
     * 描述信息
     */
    private String description;

    /**
     * 副本数量
     */
    private int replicas;
}

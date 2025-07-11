package ai.paas.beidian.pojo.request;

import lombok.*;

/**
 * 推理服务副本更新请求参数
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class InferenceUpdateReplicasRequest {

    /**
     * 副本数量
     */
    private int replicas;
}

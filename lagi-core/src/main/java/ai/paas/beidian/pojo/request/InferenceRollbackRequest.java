package ai.paas.beidian.pojo.request;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * 推理服务回滚请求参数
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InferenceRollbackRequest {

    /**
     * 版本ID
     */
    private int versionId;
}

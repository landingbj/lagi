package ai.paas.beidian.pojo.response;

import lombok.*;

/**
 * 推理服务升级检查响应数据
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class InferenceUpgradeCheckData {

    /**
     * 是否更新当前版本
     */
    private boolean updateCurrentVersion;
}

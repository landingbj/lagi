package ai.paas.beidian.pojo.response;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 请求参数：用于推理升级数据
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class InferenceData {
    /**
     * 推理服务 ID
     */
    private String inferenceId;

    /**
     * 版本 ID
     */
    private String versionId;
}

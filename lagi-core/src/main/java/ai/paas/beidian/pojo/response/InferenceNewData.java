package ai.paas.beidian.pojo.response;

import lombok.*;

/**
 * 推理服务新建响应数据
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class InferenceNewData {

    /**
     * 推理服务ID
     */
    private String inferenceId;

    /**
     * 版本ID
     */
    private int versionId;
}

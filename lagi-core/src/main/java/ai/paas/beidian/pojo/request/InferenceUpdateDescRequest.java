package ai.paas.beidian.pojo.request;

import lombok.*;

/**
 * 推理服务描述更新请求参数
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class InferenceUpdateDescRequest {

    /**
     * 描述信息
     */
    private String description;
}

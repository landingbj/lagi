package ai.paas.beidian.pojo.response;

import ai.paas.beidian.pojo.InferenceServiceItem;
import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 推理服务列表响应数据
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InferenceServiceListData {

    /**
     * 总数量
     */
    private int total;

    /**
     * 服务项列表
     */
    private List<InferenceServiceItem> items;


}

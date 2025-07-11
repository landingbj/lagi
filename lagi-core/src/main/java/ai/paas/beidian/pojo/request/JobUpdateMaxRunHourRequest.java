package ai.paas.beidian.pojo.request;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 请求参数：用于更新任务最大运行小时数
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class JobUpdateMaxRunHourRequest {
    /**
     * 最大运行小时数
     */
    private int maxRunHour;
}

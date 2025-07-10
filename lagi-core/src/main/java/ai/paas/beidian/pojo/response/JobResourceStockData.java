package ai.paas.beidian.pojo.response;

import ai.paas.beidian.pojo.GpuInfo;
import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class JobResourceStockData {
    private int mainResourceLimit;
    private int gpuCount;
    private int cpu;
    private long memory; // 根据数值 19117 可能是 MB 或者 KB，建议用 long 避免溢出
    private String orionScheduleRule;
    private int leftGpuRatioCount;
    private List<GpuInfo> gpus;
}

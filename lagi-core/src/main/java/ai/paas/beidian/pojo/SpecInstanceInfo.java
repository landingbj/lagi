package ai.paas.beidian.pojo;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class SpecInstanceInfo {
    private int cpu;
    private int memory;
    private int storage;
    private String gpuType;
    private int gpu;
    private String vgpuType;
    private int vgpu;
    private int vgpuMemory;
    private int vgpuRatio;
    private long specInstanceId;
    private String specInstanceName;
    private String deviceType;
    private String npuTemplate;
    private double price;
    private String specSeries;
}


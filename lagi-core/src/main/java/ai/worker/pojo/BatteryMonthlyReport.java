package ai.worker.pojo;

import lombok.*;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class BatteryMonthlyReport {
    private Integer serialNumber;      // 序号
    private String licensePlate;       // 车牌号
    private String selfNumber;         // 自编号
    private String vehicleModel;       // 车型
    private String manufacturer;       // 生产厂家
    private BigDecimal mileage;        // 行驶里程（公里）
    private Integer chargeCount;       // 多充次数
    private BigDecimal electricityRatio; // 用电占比
    private BigDecimal chargeKilowatt; // 充电度数
    private BigDecimal electricityPerKm; // 每公里耗电
    private BigDecimal estimatedPower; // 估算电量
    private BigDecimal nominalPower;   // 标称电量
    private BigDecimal healthDegree;   // 健康度
    private String month;              // 月份
    private Integer repairCount;       // 维修次数
    private Integer maintenanceCount;  // 保养次数
}

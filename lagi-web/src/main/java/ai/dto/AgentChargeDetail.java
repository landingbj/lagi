package ai.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class AgentChargeDetail {
    private String seq;
    private String userId;
    private Integer agentId;
    private BigDecimal amount;
    private Date time;
    private Integer status;
}

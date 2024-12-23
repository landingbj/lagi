package ai.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PrepayRequest {
    private String lagiUserId;
    private Integer agentId;
    private String fee;
}

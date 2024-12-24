package ai.servlet.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class LagiAgentExpense {
    private Integer id;
    private String userId;
    private Integer agentId;
    private BigDecimal balance;
}

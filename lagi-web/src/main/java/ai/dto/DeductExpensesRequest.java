package ai.dto;

import lombok.Data;

@Data
public class DeductExpensesRequest {
    private String userId;
    private Integer agentId;
}

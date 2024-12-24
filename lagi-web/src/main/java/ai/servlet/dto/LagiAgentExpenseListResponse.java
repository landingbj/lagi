package ai.servlet.dto;

import lombok.Data;

import java.util.List;

@Data
public class LagiAgentExpenseListResponse {
    private String status;
    private List<LagiAgentExpense> data;
}

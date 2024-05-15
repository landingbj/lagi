package ai.servlet.dto;

import lombok.Data;

@Data
public class NextPromptRequest {
    private String action;
    private String prompt;
}

package ai.worker.pojo;

import lombok.Data;

@Data
public class AuditPrompt {
    private String keyword;
    private String searchStr;
    private String prompt;
}

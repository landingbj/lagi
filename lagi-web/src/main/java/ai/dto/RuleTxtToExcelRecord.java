package ai.dto;

import lombok.Data;

@Data
public class RuleTxtToExcelRecord {
    private String rule;
    private String no;
    private String block = "";
    private String reason;
    private String decision = "";
}

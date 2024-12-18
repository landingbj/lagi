package ai.servlet.dto;

import lombok.Data;

import java.util.Date;

@Data
public class CompanyIncomingDocumentsRequest {
    private String level;

    private String id;     // 文档ID
    private String serialNumber;   // 流程编号
    private Date receiptDate;      // 收文日期
    private String documentNumber; // 文档编号
    private String issuingUnit;    // 发文单位
    private String documentTitle;  // 文档标题
}

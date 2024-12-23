package ai.dto;

import lombok.Data;

@Data
public class PrepayResponse {
    private String outTradeNo;
    private String qrCode;
    private String result;
    private String totalFee;
}

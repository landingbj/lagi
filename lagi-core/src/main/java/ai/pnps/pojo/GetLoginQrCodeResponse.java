package ai.pnps.pojo;

import lombok.Data;

@Data
public class GetLoginQrCodeResponse {
    private String image_url;
    private int status;
}

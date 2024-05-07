package ai.agent.pojo;

import lombok.Data;

@Data
public class GetLoginQrCodeResponse {
    private String imageUrl;
    private int status;
}

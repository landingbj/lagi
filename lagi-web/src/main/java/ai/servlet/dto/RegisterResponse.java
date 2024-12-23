package ai.servlet.dto;

import lombok.Data;

@Data
public class RegisterResponse {
    private String status;
    private String channelId;
    private String msg;
}

package ai.servlet.dto;

import lombok.Data;

@Data
public class LoginRequest {
    private String username;
    private String password;
    private String captcha;
}

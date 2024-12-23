package ai.servlet.dto;

import lombok.Data;

@Data
public class RegisterRequest {
    private String username;
    private String domainName;
    private String password;
    private String captcha;
}

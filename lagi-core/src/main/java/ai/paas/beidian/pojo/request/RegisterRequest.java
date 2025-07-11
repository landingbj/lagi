package ai.paas.beidian.pojo.request;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class RegisterRequest {
    private String displayName;
    private String password;
    private String phone;
    private String verificationCode;
    private String inviteCode;
    private String ticket;
    private String randstr;
}

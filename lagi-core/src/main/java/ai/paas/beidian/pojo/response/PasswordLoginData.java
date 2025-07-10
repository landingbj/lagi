package ai.paas.beidian.pojo.response;

import lombok.*;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class PasswordLoginData {
    private String token;
    private String refreshToken;
    private String tokenExpireTime;
    private String userId;
    private String loginMethod;
    private String userName;
    private String displayName;
    private List<String> permissionList;
    private List<String> roles;
    private List<Integer> roleIds;
}

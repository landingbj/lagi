package ai.account;

import lombok.Builder;
import lombok.Data;

/** Read-only projection of a Cuihua-owned account in the shared tokenlink database. */
@Data
@Builder
public class CuihuaUser {
    private String userId;
    private String username;
    private String passwordHash;
    private String role;
    private String userLoginStatus;

    public boolean isSystemAdmin() {
        return role != null && "system_admin".equalsIgnoreCase(role.trim());
    }
}

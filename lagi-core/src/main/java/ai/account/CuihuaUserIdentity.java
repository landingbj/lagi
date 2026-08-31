package ai.account;

import lombok.Builder;
import lombok.Data;

/** Read-only identity fields used by presentation-layer usage reports. */
@Data
@Builder
public class CuihuaUserIdentity {
    private String userId;
    private String username;
    private String displayName;
}

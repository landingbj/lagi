package ai.account;

import lombok.Builder;
import lombok.Data;

/** A LinkMind-owned local API key associated with one Cuihua user. */
@Data
@Builder
public class LocalUserApiKey {
    private Long id;
    private String userId;
    /** Present only immediately after a successful create operation. */
    private String apiKey;
    private String keyHint;
    private String remark;
    private String status;
    private Long createdAt;
    private Long lastUsedAt;
    private Long revokedAt;

    public boolean isActive() {
        return "active".equalsIgnoreCase(status);
    }
}

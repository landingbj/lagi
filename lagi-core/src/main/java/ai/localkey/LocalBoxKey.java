package ai.localkey;

import lombok.Builder;
import lombok.Data;

/** Local box credential metadata. The full key is returned only on create/authenticate. */
@Data
@Builder
public class LocalBoxKey {
    private Long id;
    private String subjectId;
    private String displayName;
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

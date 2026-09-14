package ai.config.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/** Optional settings for the self-contained local box key service. */
@Data
public class LocalKeyConfig {
    private Boolean enable = true;

    /**
     * Protects local-key management endpoints. When omitted, management is
     * limited to loopback requests so a fresh box is not exposed on the LAN.
     */
    @JsonProperty("admin_key")
    private String adminKey;

    /** Name of the configured Landing backend used for cloud fallback. */
    @JsonProperty("cloud_backend")
    private String cloudBackend = "landing";
}

package ai.config.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class GeneralConfig {
    /**
     * Legacy Agent Mate/Server configuration. It is read for compatibility only
     * and no longer controls LinkMind behavior.
     */
    @Deprecated
    private String mode;

    @JsonProperty("local_api_key_editable")
    private Boolean localApiKeyEditable = true;

    @JsonProperty("cascade_api_address")
    private String cascadeApiAddress;

    @JsonProperty("menu_login_required")
    private Boolean menuLoginRequired = false;

    @JsonProperty("container_url")
    private String containerUrl;

    @JsonProperty("local_key")
    private LocalKeyConfig localKey = new LocalKeyConfig();
}

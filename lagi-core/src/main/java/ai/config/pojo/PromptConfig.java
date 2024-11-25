package ai.config.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.List;

@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@Data
public class PromptConfig {
    @JsonProperty("prompt")
    private Prompt prompt;
    @Data
    public static class Prompt {
        private Boolean enable;
        private List<Role> roles;
    }
    @Data
    public static class Role {
        private String name;
        private String prompt;
    }

}

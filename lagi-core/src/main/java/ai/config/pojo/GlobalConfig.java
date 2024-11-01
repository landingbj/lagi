package ai.config.pojo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.ToString;

@ToString
@Data
public class GlobalConfig {
    @JsonProperty("chat")
    private ChatConfig chat;

    @JsonCreator
    public GlobalConfig() {
        if(chat == null) {
            chat = new ChatConfig();
        }
    }
}

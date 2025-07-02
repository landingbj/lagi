package ai.config.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Builder
@ToString
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PnpConfig {
    private String name;
    @JsonProperty("driver")
    private String driver;
    private String apiKey;
}

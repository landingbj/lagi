package ai.config.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.math.BigDecimal;

@Builder
@ToString
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgentConfig {
    private Integer id;
    private String lagiUserId;
    private String name;
    @JsonProperty("driver")
    private String driver;
    private String apiKey;
    private String token;
    private String appId;
    private String userId;
    private String wrongCase;
    private String endpoint;
    private Boolean isFeeRequired;
    private BigDecimal pricePerReq;
}

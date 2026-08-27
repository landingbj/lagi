package ai.llm.pojo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenStatisticsDetail {
    private long id;
    /** Record time (epoch millis). */
    private long createdAt;
    private long promptTokens;
    private long completionTokens;
    private long totalTokens;
    private long savedTokens;
    private String provider;
    private String model;
    private String userId;
    /** Current TokenLink display name, with username/user ID fallback applied by the API. */
    private String userDisplayName;
    private Long userApiKeyId;
    private BigDecimal inputCost;
    private BigDecimal outputCost;
    private BigDecimal totalCost;
    private String currency;
    private String priceSource;
}

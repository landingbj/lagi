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
public class TokenStatisticsSessionItem {
    private String sessionId;
    private long requestCount;
    private long promptTokens;
    private long completionTokens;
    private long totalTokens;
    private long savedTokens;
    /**
     * @deprecated Use {@link #totalCost} and {@link #currency}.
     */
    @Deprecated
    private double estimatedCost;
    private BigDecimal totalCost;
    private String currency;
    private long pricedRecordCount;
    private long unpricedRecordCount;
    private long firstRequestAt;
    private long lastRequestAt;
}

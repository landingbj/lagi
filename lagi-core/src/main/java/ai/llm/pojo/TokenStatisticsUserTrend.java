package ai.llm.pojo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/** One calendar bucket of usage, initially grouped by user for later identity filtering. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenStatisticsUserTrend {
    private String bucket;
    private String userId;
    private long requestCount;
    private long totalTokens;
    private BigDecimal totalCost;
    private String currency;
    private long pricedRecordCount;
    private long unpricedRecordCount;

    public void addUsage(TokenStatisticsUserTrend other) {
        if (other == null) {
            return;
        }
        requestCount += other.requestCount;
        totalTokens += other.totalTokens;
        pricedRecordCount += other.pricedRecordCount;
        unpricedRecordCount += other.unpricedRecordCount;
        if (other.totalCost != null) {
            totalCost = totalCost == null ? other.totalCost : totalCost.add(other.totalCost);
        }
        if (currency == null) {
            currency = other.currency;
        } else if (other.currency != null && !currency.equalsIgnoreCase(other.currency)) {
            currency = null;
        }
    }
}

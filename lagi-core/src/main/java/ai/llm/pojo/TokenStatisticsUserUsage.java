package ai.llm.pojo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/** Usage totals for one attributed user or the synthetic unattributed row. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenStatisticsUserUsage {
    private String userId;
    private String userDisplayName;
    private String username;
    private boolean unattributed;
    private long requestCount;
    private long modelCount;
    private long promptTokens;
    private long completionTokens;
    private long totalTokens;
    private BigDecimal totalCost;
    private String currency;
    private long pricedRecordCount;
    private long unpricedRecordCount;

    public void addUsage(TokenStatisticsUserUsage other) {
        if (other == null) {
            return;
        }
        requestCount += other.requestCount;
        promptTokens += other.promptTokens;
        completionTokens += other.completionTokens;
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

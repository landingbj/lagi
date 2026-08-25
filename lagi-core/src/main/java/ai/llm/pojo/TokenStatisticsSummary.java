package ai.llm.pojo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Aggregates for a time window: total usage, total saved, daily average (total usage / calendar days, rounded).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenStatisticsSummary {
    private TokenStatisticsRange range;
    /** Sum of {@code total_tokens} in the window. */
    private long totalTokensConsumed;
    /** Sum of {@code saved_tokens} in the window. */
    private long totalSavedTokens;
    /** Average token usage per calendar day in the window. */
    private long dailyAvgTokensConsumed;
    /** Number of detail rows aggregated. */
    private long recordCount;
    private long totalPromptTokens;
    private long totalCompletionTokens;
    /** Present only when all priced records in the range use one currency. */
    private BigDecimal totalCost;
    private String currency;
    private long pricedRecordCount;
    private long unpricedRecordCount;
    private List<TokenStatisticsCurrencyCost> costsByCurrency;
}

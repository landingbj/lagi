package ai.llm.pojo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Compact aggregate for APIs: sums, averages, and row count in the time window.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenStatisticsOverview {
    /** Echo of the requested range key (e.g. {@code 7d}, {@code all}). */
    private String range;
    /** Sum of {@code total_tokens} in the window. */
    private long totalTokens;
    /** Sum of {@code saved_tokens} in the window. */
    private long totalSavedTokens;
    /** Average token usage per calendar day in the window (0 for {@link ai.llm.pojo.TokenStatisticsRange#ALL}). */
    private long dailyAvgTokens;
    /** Number of stored usage rows in the window. */
    private long recordCount;
    private long totalPromptTokens;
    private long totalCompletionTokens;
    /** Present only when priced usage in the range uses one currency. */
    private BigDecimal totalCost;
    private String currency;
    private long pricedRecordCount;
    private long unpricedRecordCount;
    /** Cost breakdown when the selected usage contains multiple currencies. */
    private List<TokenStatisticsCurrencyCost> costsByCurrency;
    /** Authorized query scope: self, user, or all. */
    private String scope;
    /** User being queried for self/user scopes; null for all-users scope. */
    private String targetUserId;
}

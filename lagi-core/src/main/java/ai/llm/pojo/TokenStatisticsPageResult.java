package ai.llm.pojo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenStatisticsPageResult {
    private TokenStatisticsRange range;
    /** One-based page index. */
    private int page;
    private int pageSize;
    private long total;
    private List<TokenStatisticsDetail> records;
    /** Authorized query scope: self, user, or all. */
    private String scope;
    /** User being queried for self/user scopes; null for all-users scope. */
    private String targetUserId;

    public static TokenStatisticsPageResult empty(TokenStatisticsRange range, int page, int pageSize) {
        return TokenStatisticsPageResult.builder()
                .range(range)
                .page(page)
                .pageSize(pageSize)
                .total(0)
                .records(Collections.emptyList())
                .build();
    }
}

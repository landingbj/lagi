package ai.llm.pojo;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/** Locally calculated cost for one completed model call. */
@Data
@Builder
public class TokenUsageCost {
    private BigDecimal inputCost;
    private BigDecimal outputCost;
    private BigDecimal totalCost;
    private String currency;
    private String priceSource;
}

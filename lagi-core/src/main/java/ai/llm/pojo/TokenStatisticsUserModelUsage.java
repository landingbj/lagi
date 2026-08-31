package ai.llm.pojo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** One distinct non-blank model used by an attributed user in a dashboard window. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenStatisticsUserModelUsage {
    private String userId;
    private String model;
}

package ai.agent.pojo;

import lombok.*;

@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
@ToString
public class CozeUsage {
    private int token_count;
    private int output_count;
    private int input_count;
}

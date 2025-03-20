package ai.openai.pojo;

import lombok.Data;

@Data
public class CompletionChoice {
    private String text;
    private Integer index;
    private LogProbResult logprobs;
    private String finish_reason;
}

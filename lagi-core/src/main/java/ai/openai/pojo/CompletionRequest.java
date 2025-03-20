package ai.openai.pojo;

import lombok.Data;

import java.util.List;
import java.util.Map;
@Data
public class CompletionRequest {
    private String model;
    private String prompt;
    private String suffix;
    private Integer max_tokens;
    private Double temperature;
    private Double top_p;
    private Integer n;
    private Boolean stream;
    private Integer logprobs;
    private Boolean echo;
    private List<String> stop;
    private Double presence_penalty;
    private Double frequency_penalty;
    private Integer best_of;
    private Map<String, Integer> logit_bias;
    private String user;
}

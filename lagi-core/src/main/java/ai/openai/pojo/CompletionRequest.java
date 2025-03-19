package ai.openai.pojo;

import java.util.List;
import java.util.Map;

/**
 * A request for OpenAi to generate a predicted completion for a prompt. All
 * fields are nullable.
 * <p>
 * https://beta.openai.com/docs/api-reference/completions/create
 */
public class CompletionRequest {

    /**
     * The name of the model to use. Required if specifying a fine tuned model
     * or if using the new v1/completions endpoint.
     */
    private String model;

    /**
     * An optional prompt to complete from
     */
    private String prompt;
    private String suffix;

    /**
     * The maximum number of tokens to generate. Requests can use up to 2048
     * tokens shared between prompt and completion. (One token is roughly 4
     * characters for normal English text)
     */
    private Integer max_tokens;

    /**
     * What sampling temperature to use. Higher values means the model will take
     * more risks. Try 0.9 for more creative applications, and 0 (argmax
     * sampling) for ones with a well-defined answer.
     * <p>
     * We generally recommend using this or {@link CompletionRequest#topP} but
     * not both.
     */
    private Double temperature;

    /**
     * An alternative to sampling with temperature, called nucleus sampling,
     * where the model considers the results of the tokens with top_p
     * probability mass. So 0.1 means only the tokens comprising the top 10%
     * probability mass are considered.
     * <p>
     * We generally recommend using this or
     * {@link CompletionRequest#temperature} but not both.
     */
    private Double top_p;

    /**
     * How many completions to generate for each prompt.
     * <p>
     * Because this parameter generates many completions, it can quickly consume
     * your token quota. Use carefully and ensure that you have reasonable
     * settings for {@link CompletionRequest#maxTokens} and
     * {@link CompletionRequest#stop}.
     */
    private Integer n;

    /**
     * Whether to stream back partial progress. If set, tokens will be sent as
     * data-only server-sent events as they become available, with the stream
     * terminated by a data: DONE message.
     */
    private Boolean stream;

    /**
     * Include the log probabilities on the logprobs most likely tokens, as well
     * the chosen tokens. For example, if logprobs is 10, the API will return a
     * list of the 10 most likely tokens. The API will always return the logprob
     * of the sampled token, so there may be up to logprobs+1 elements in the
     * response.
     */
    private Integer logprobs;

    /**
     * Echo back the prompt in addition to the completion
     */
    private Boolean echo;

    /**
     * Up to 4 sequences where the API will stop generating further tokens. The
     * returned text will not contain the stop sequence.
     */
    private List<String> stop;

    /**
     * Number between 0 and 1 (default 0) that penalizes new tokens based on
     * whether they appear in the text so far. Increases the model's likelihood
     * to talk about new topics.
     */
    private Double presence_penalty;

    /**
     * Number between 0 and 1 (default 0) that penalizes new tokens based on
     * their existing frequency in the text so far. Decreases the model's
     * likelihood to repeat the same line verbatim.
     */
    private Double frequency_penalty;

    /**
     * Generates best_of completions server-side and returns the "best" (the one
     * with the lowest log probability per token). Results cannot be streamed.
     * <p>
     * When used with {@link CompletionRequest#n}, best_of controls the number
     * of candidate completions and n specifies how many to return, best_of must
     * be greater than n.
     */
    private Integer best_of;

    /**
     * Modify the likelihood of specified tokens appearing in the completion.
     * <p>
     * Maps tokens (specified by their token ID in the GPT tokenizer) to an
     * associated bias value from -100 to 100.
     * <p>
     * https://beta.openai.com/docs/api-reference/completions/create#completions
     * /create-logit_bias
     */
    private Map<String, Integer> logit_bias;

    /**
     * A unique identifier representing your end-user, which will help OpenAI to
     * monitor and detect abuse.
     */
    private String user;

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public Integer getMaxTokens() {
        return max_tokens;
    }

    public void setMaxTokens(Integer maxTokens) {
        this.max_tokens = maxTokens;
    }

    public Double getTemperature() {
        return temperature;
    }

    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }

    public Double getTopP() {
        return top_p;
    }

    public void setTopP(Double topP) {
        this.top_p = topP;
    }

    public Integer getN() {
        return n;
    }

    public void setN(Integer n) {
        this.n = n;
    }

    public Boolean getStream() {
        return stream;
    }

    public void setStream(Boolean stream) {
        this.stream = stream;
    }

    public Integer getLogprobs() {
        return logprobs;
    }

    public void setLogprobs(Integer logprobs) {
        this.logprobs = logprobs;
    }

    public Boolean getEcho() {
        return echo;
    }

    public void setEcho(Boolean echo) {
        this.echo = echo;
    }

    public List<String> getStop() {
        return stop;
    }

    public void setStop(List<String> stop) {
        this.stop = stop;
    }

    public Double getPresencePenalty() {
        return presence_penalty;
    }

    public void setPresencePenalty(Double presencePenalty) {
        this.presence_penalty = presencePenalty;
    }

    public Double getFrequencyPenalty() {
        return frequency_penalty;
    }

    public void setFrequencyPenalty(Double frequencyPenalty) {
        this.frequency_penalty = frequencyPenalty;
    }

    public Integer getBestOf() {
        return best_of;
    }

    public void setBestOf(Integer bestOf) {
        this.best_of = bestOf;
    }

    public Map<String, Integer> getLogitBias() {
        return logit_bias;
    }

    public void setLogitBias(Map<String, Integer> logitBias) {
        this.logit_bias = logitBias;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    @Override
    public String toString() {
        return "CompletionRequest [model=" + model + ", prompt=" + prompt + ", suffix=" + suffix + ", max_tokens=" + max_tokens + ", temperature=" + temperature + ", top_p=" + top_p + ", n=" + n + ", stream=" + stream + ", logprobs=" + logprobs + ", echo=" + echo + ", stop=" + stop + ", presence_penalty=" + presence_penalty + ", frequency_penalty=" + frequency_penalty + ", best_of=" + best_of + ", logit_bias=" + logit_bias + ", user=" + user + "]";
    }
}

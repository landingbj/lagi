package ai.openai.pojo;

import java.util.List;
import java.util.Map;

/**
 * Log probabilities of different token options Returned if
 * {@link CompletionRequest#logprobs} is greater than zero
 * <p>
 * https://beta.openai.com/docs/api-reference/create-completion
 */
public class LogProbResult {

    /**
     * The tokens chosen by the completion api
     */
    private List<String> tokens;

    /**
     * The log probability of each token in {@link tokens}
     */
    private List<Double> tokenLogprobs;

    /**
     * A map for each index in the completion result. The map contains the top
     * {@link CompletionRequest#logprobs} tokens and their probabilities
     */
    private List<Map<String, Double>> topLogprobs;

    /**
     * The character offset from the start of the returned text for each of the
     * chosen tokens.
     */
    private List<Integer> textOffset;

    public List<String> getTokens() {
        return tokens;
    }

    public void setTokens(List<String> tokens) {
        this.tokens = tokens;
    }

    public List<Double> getTokenLogprobs() {
        return tokenLogprobs;
    }

    public void setTokenLogprobs(List<Double> tokenLogprobs) {
        this.tokenLogprobs = tokenLogprobs;
    }

    public List<Map<String, Double>> getTopLogprobs() {
        return topLogprobs;
    }

    public void setTopLogprobs(List<Map<String, Double>> topLogprobs) {
        this.topLogprobs = topLogprobs;
    }

    public List<Integer> getTextOffset() {
        return textOffset;
    }

    public void setTextOffset(List<Integer> textOffset) {
        this.textOffset = textOffset;
    }

    @Override
    public String toString() {
        return "LogProbResult [tokens=" + tokens + ", tokenLogprobs=" + tokenLogprobs + ", topLogprobs=" + topLogprobs + ", textOffset=" + textOffset + "]";
    }
}

package ai.llm.pojo;

public class QwenUsage {
    public Integer total_tokens;
    public Integer output_tokens;
    public Integer input_tokens;

    public Integer getTotal_tokens() {
        return total_tokens;
    }

    public void setTotal_tokens(Integer total_tokens) {
        this.total_tokens = total_tokens;
    }

    public Integer getOutput_tokens() {
        return output_tokens;
    }

    public void setOutput_tokens(Integer output_tokens) {
        this.output_tokens = output_tokens;
    }

    public Integer getInput_tokens() {
        return input_tokens;
    }

    public void setInput_tokens(Integer input_tokens) {
        this.input_tokens = input_tokens;
    }

    @Override
    public String toString() {
        return "QwenUsage [total_tokens=" + total_tokens + ", output_tokens=" + output_tokens + ", input_tokens="
                + input_tokens + "]";
    }

}

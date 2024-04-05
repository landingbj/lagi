package ai.lagi.pojo;

public class QwenParameters {
    private String result_format;
    private Integer seed;
    private Integer max_tokens;
    private Double top_p;
    private Integer top_k;
    private Double repetition_penalty;
    private Double temperature;
    private String stop;
    private Boolean enable_search;
    private Boolean incremental_output;

    public String getResult_format() {
        return result_format;
    }

    public void setResult_format(String result_format) {
        this.result_format = result_format;
    }

    public Integer getSeed() {
        return seed;
    }

    public void setSeed(Integer seed) {
        this.seed = seed;
    }

    public Integer getMax_tokens() {
        return max_tokens;
    }

    public void setMax_tokens(Integer max_tokens) {
        this.max_tokens = max_tokens;
    }

    public Double getTop_p() {
        return top_p;
    }

    public void setTop_p(Double top_p) {
        this.top_p = top_p;
    }

    public Integer getTop_k() {
        return top_k;
    }

    public void setTop_k(Integer top_k) {
        this.top_k = top_k;
    }

    public Double getRepetition_penalty() {
        return repetition_penalty;
    }

    public void setRepetition_penalty(Double repetition_penalty) {
        this.repetition_penalty = repetition_penalty;
    }

    public Double getTemperature() {
        return temperature;
    }

    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }

    public String getStop() {
        return stop;
    }

    public void setStop(String stop) {
        this.stop = stop;
    }

    public Boolean getEnable_search() {
        return enable_search;
    }

    public void setEnable_search(Boolean enable_search) {
        this.enable_search = enable_search;
    }

    public Boolean getIncremental_output() {
        return incremental_output;
    }

    public void setIncremental_output(Boolean incremental_output) {
        this.incremental_output = incremental_output;
    }

    @Override
    public String toString() {
        return "QwenParameters [result_format=" + result_format + ", seed=" + seed + ", max_tokens=" + max_tokens
                + ", top_p=" + top_p + ", top_k=" + top_k + ", repetition_penalty=" + repetition_penalty
                + ", temperature=" + temperature + ", stop=" + stop + ", enable_search=" + enable_search
                + ", incremental_output=" + incremental_output + "]";
    }

}

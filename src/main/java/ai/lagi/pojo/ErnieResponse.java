package ai.lagi.pojo;

public class ErnieResponse {
    private String id;
    private String object;
    private Long created;
    private String result;
    private Boolean is_truncated;
    private Boolean need_clear_history;
    private Usage usage;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getObject() {
        return object;
    }

    public void setObject(String object) {
        this.object = object;
    }

    public Long getCreated() {
        return created;
    }

    public void setCreated(Long created) {
        this.created = created;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public boolean isIs_truncated() {
        return is_truncated;
    }

    public void setIs_truncated(Boolean is_truncated) {
        this.is_truncated = is_truncated;
    }

    public boolean isNeed_clear_history() {
        return need_clear_history;
    }

    public void setNeed_clear_history(Boolean need_clear_history) {
        this.need_clear_history = need_clear_history;
    }

    public Usage getUsage() {
        return usage;
    }

    public void setUsage(Usage usage) {
        this.usage = usage;
    }

    public static class Usage {
        private Integer prompt_tokens;
        private Integer completion_tokens;
        private Integer total_tokens;

        public Integer getPrompt_tokens() {
            return prompt_tokens;
        }

        public void setPrompt_tokens(Integer prompt_tokens) {
            this.prompt_tokens = prompt_tokens;
        }

        public Integer getCompletion_tokens() {
            return completion_tokens;
        }

        public void setCompletion_tokens(Integer completion_tokens) {
            this.completion_tokens = completion_tokens;
        }

        public Integer getTotal_tokens() {
            return total_tokens;
        }

        public void setTotal_tokens(Integer total_tokens) {
            this.total_tokens = total_tokens;
        }
    }

    @Override
    public String toString() {
        return "ErnieResponse [id=" + id + ", object=" + object + ", created=" + created + ", result=" + result
                + ", is_truncated=" + is_truncated + ", need_clear_history=" + need_clear_history + ", usage=" + usage
                + "]";
    }
}

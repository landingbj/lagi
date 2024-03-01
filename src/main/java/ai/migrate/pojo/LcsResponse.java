package ai.migrate.pojo;

public class LcsResponse {
    private String status;
    private Double similarity;
    private String answer;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Double getSimilarity() {
        return similarity;
    }

    public void setSimilarity(Double similarity) {
        this.similarity = similarity;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    @Override
    public String toString() {
        return "Response [status=" + status + ", similarity=" + similarity + ", answer=" + answer + "]";
    }

}

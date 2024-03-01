package ai.migrate.pojo;

public class IndexSearchRequest {
    private String text;
    private String category;
    private Integer similarity_top_k;
    private Integer similarity_cutoff;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Integer getSimilarity_top_k() {
        return similarity_top_k;
    }

    public void setSimilarity_top_k(Integer similarity_top_k) {
        this.similarity_top_k = similarity_top_k;
    }

    public Integer getSimilarity_cutoff() {
        return similarity_cutoff;
    }

    public void setSimilarity_cutoff(Integer similarity_cutoff) {
        this.similarity_cutoff = similarity_cutoff;
    }

    @Override
    public String toString() {
        return "IndexSearchRequest [text=" + text + ", category=" + category + ", similarity_top_k=" + similarity_top_k
                + ", similarity_cutoff=" + similarity_cutoff + "]";
    }
}

package ai.openai.pojo;

import java.util.List;

public class ChatResponseWithContext {
    private String indexId;
    private String text;
    private String category;
    private List<String> filename;
    private List<String> filepath;
    private String author;
    private Double distance;
    private String context;
    private String image;

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getIndexId() {
        return indexId;
    }

    public void setIndexId(String indexId) {
        this.indexId = indexId;
    }

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

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public Double getDistance() {
        return distance;
    }

    public void setDistance(Double distance) {
        this.distance = distance;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public List<String> getFilename() {
        return filename;
    }

    public void setFilename(List<String> filename) {
        this.filename = filename;
    }

    public List<String> getFilepath() {
        return filepath;
    }

    public void setFilepath(List<String> filepath) {
        this.filepath = filepath;
    }

    @Override
    public String toString() {
        return "ChatResponseWithContext [indexId=" + indexId + ", text=" + text + ", category=" + category
                + ", filename=" + filename + ", filepath=" + filepath + ", author=" + author + ", distance=" + distance
                + ", context=" + context + ", image=" + image + "]";
    }
}

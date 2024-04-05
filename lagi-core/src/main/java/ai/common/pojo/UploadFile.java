package ai.common.pojo;

public class UploadFile {
    private String fileId;
    private String filename;
    private String filepath;
    private String category;

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getFilepath() {
        return filepath;
    }

    public void setFilepath(String filepath) {
        this.filepath = filepath;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    @Override
    public String toString() {
        return "UploadFile [fileId=" + fileId + ", filename=" + filename + ", filepath=" + filepath + ", category="
                + category + "]";
    }
}

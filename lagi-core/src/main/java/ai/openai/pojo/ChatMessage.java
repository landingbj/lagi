package ai.openai.pojo;

import java.util.List;

public class ChatMessage {
    private String role;
    private String content;
    private String reasoning_content;
    private List<String> filename;
    private List<String> filepath;
    private String author;
    private Float distance;
    private String image;
    private List<String> imageList;
    private String context;
    private List<String> contextChunkIds;
    private List<ToolCall> tool_calls;

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

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public Float getDistance() {
        return distance;
    }

    public void setDistance(Float distance) {
        this.distance = distance;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public List<String> getImageList() {
        return imageList;
    }

    public void setImageList(List<String> imageList) {
        this.imageList = imageList;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public List<ToolCall> getTool_calls() {
        return tool_calls;
    }

    public void setTool_calls(List<ToolCall> tool_calls) {
        this.tool_calls = tool_calls;
    }

    public String getReasoning_content() {
        return reasoning_content;
    }

    public void setReasoning_content(String reasoning_content) {
        this.reasoning_content = reasoning_content;
    }

    public List<String> getContextChunkIds() {
        return contextChunkIds;
    }

    public void setContextChunkIds(List<String> contextChunkIds) {
        this.contextChunkIds = contextChunkIds;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((author == null) ? 0 : author.hashCode());
        result = prime * result + ((content == null) ? 0 : content.hashCode());
        result = prime * result + ((context == null) ? 0 : context.hashCode());
        result = prime * result + ((contextChunkIds == null) ? 0 : contextChunkIds.hashCode());
        result = prime * result + ((distance == null) ? 0 : distance.hashCode());
        result = prime * result + ((filename == null) ? 0 : filename.hashCode());
        result = prime * result + ((filepath == null) ? 0 : filepath.hashCode());
        result = prime * result + ((image == null) ? 0 : image.hashCode());
        result = prime * result + ((imageList == null) ? 0 : imageList.hashCode());
        result = prime * result + ((reasoning_content == null) ? 0 : reasoning_content.hashCode());
        result = prime * result + ((role == null) ? 0 : role.hashCode());
        result = prime * result + ((tool_calls == null) ? 0 : tool_calls.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ChatMessage other = (ChatMessage) obj;
        if (author == null) {
            if (other.author != null)
                return false;
        } else if (!author.equals(other.author))
            return false;
        if (content == null) {
            if (other.content != null)
                return false;
        } else if (!content.equals(other.content))
            return false;
        if (context == null) {
            if (other.context != null)
                return false;
        } else if (!context.equals(other.context))
            return false;
        if (contextChunkIds == null) {
            if (other.contextChunkIds != null)
                return false;
        } else if (!contextChunkIds.equals(other.contextChunkIds))
            return false;
        if (distance == null) {
            if (other.distance != null)
                return false;
        } else if (!distance.equals(other.distance))
            return false;
        if (filename == null) {
            if (other.filename != null)
                return false;
        } else if (!filename.equals(other.filename))
            return false;
        if (filepath == null) {
            if (other.filepath != null)
                return false;
        } else if (!filepath.equals(other.filepath))
            return false;
        if (image == null) {
            if (other.image != null)
                return false;
        } else if (!image.equals(other.image))
            return false;
        if (imageList == null) {
            if (other.imageList != null)
                return false;
        } else if (!imageList.equals(other.imageList))
            return false;
        if (reasoning_content == null) {
            if (other.reasoning_content != null)
                return false;
        } else if (!reasoning_content.equals(other.reasoning_content))
            return false;
        if (role == null) {
            if (other.role != null)
                return false;
        } else if (!role.equals(other.role))
            return false;
        if (tool_calls == null) {
            return other.tool_calls == null;
        } else return tool_calls.equals(other.tool_calls);
    }

    @Override
    public String toString() {
        return "ChatMessage [role=" + role + ", content=" + content + ", reasoning_content=" + reasoning_content
                + ", filename=" + filename + ", filepath=" + filepath + ", author=" + author + ", distance=" + distance
                + ", image=" + image + ", imageList=" + imageList + ", context=" + context + ", contextChunkIds="
                + contextChunkIds + ", tool_calls=" + tool_calls + "]";
    }
}

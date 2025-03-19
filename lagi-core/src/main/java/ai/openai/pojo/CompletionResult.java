package ai.openai.pojo;

import java.util.List;

/**
 * An object containing a response from the completion api
 * <p>
 * https://beta.openai.com/docs/api-reference/completions/create
 */
public class CompletionResult {
    /**
     * A unique id assigned to this completion.
     */
    private String id;

    /**
     * The type of object returned, should be "text_completion"
     */
    private String object;

    /**
     * The creation time in epoch seconds.
     */
    private long created;

    /**
     * The GPT-3 model used.
     */
    private String model;

    /**
     * A list of generated completions.
     */
    private List<CompletionChoice> choices;

    /**
     * The API usage for this request
     */
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

    public long getCreated() {
        return created;
    }

    public void setCreated(long created) {
        this.created = created;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public List<CompletionChoice> getChoices() {
        return choices;
    }

    public void setChoices(List<CompletionChoice> choices) {
        this.choices = choices;
    }

    public Usage getUsage() {
        return usage;
    }

    public void setUsage(Usage usage) {
        this.usage = usage;
    }

    @Override
    public String toString() {
        return "CompletionResult [id=" + id + ", object=" + object + ", created=" + created + ", model=" + model + ", choices=" + choices + ", usage=" + usage + "]";
    }
}

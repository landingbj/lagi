package ai.openai.pojo;

public class Function {
    private String name;
    private String description;
    private Boolean strict = Boolean.TRUE;
    private Parameters parameters;

    public String getName() {
        return name;
    }

    public void setName(String value) {
        this.name = value;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String value) {
        this.description = value;
    }

    public Boolean getStrict() {
        return strict;
    }

    public void setStrict(Boolean strict) {
        this.strict = strict;
    }

    public Parameters getParameters() {
        return parameters;
    }

    public void setParameters(Parameters value) {
        this.parameters = value;
    }

    @Override
    public String toString() {
        return "Function [name=" + name + ", description=" + description
                + ", strict=" + strict + ", parameters=" + parameters + "]";
    }


}

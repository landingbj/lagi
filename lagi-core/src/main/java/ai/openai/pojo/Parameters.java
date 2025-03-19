package ai.openai.pojo;

import java.util.List;
import java.util.Map;

public class Parameters {
    private String type;
    private Map<String, Property> properties;
    private List<String> required;
    private Boolean additionalProperties = false;


    public String getType() {
        return type;
    }

    public void setType(String value) {
        this.type = value;
    }


    public Map<String, Property> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Property> value) {
        this.properties = value;
    }


    public List<String> getRequired() {
        return required;
    }

    public void setRequired(List<String> value) {
        this.required = value;
    }


    public Boolean getAdditionalProperties() {
        return additionalProperties;
    }

    public void setAdditionalProperties(boolean value) {
        this.additionalProperties = value;
    }

    @Override
    public String toString() {
        return "Parameters [type=" + type + ", properties=" + properties
                + ", required=" + required + ", additionalProperties="
                + additionalProperties + "]";
    }


}

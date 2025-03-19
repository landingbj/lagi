package ai.openai.pojo;

import java.util.List;

public class Property {
    private String type;
    private String description;
    private List<String> enums;

    public String getType() {
        return type;
    }

    public void setType(String value) {
        this.type = value;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String value) {
        this.description = value;
    }

    public List<String> getEnums() {
        return enums;
    }

    public void setEnums(List<String> enums) {
        this.enums = enums;
    }

    @Override
    public String toString() {
        return "OrderID [type=" + type + ", description=" + description
                + ", enums=" + enums + "]";
    }

}

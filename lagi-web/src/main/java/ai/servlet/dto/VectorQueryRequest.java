package ai.servlet.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.HashMap;
import java.util.Map;
@Data
@EqualsAndHashCode
public class VectorQueryRequest {
    private String userId;
    private String text;
    private Integer n;
    private Map<String, String> where = new HashMap<>();
    private String category;

    @Override
    public String toString() {
        return "VectorQueryRequest{" + "text='" + text + '\'' + "," +
                " n=" + n + ", where=" + where + ", category='" + category + '\'' + '}';
    }
}

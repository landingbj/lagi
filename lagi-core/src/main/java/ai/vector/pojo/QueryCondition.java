package ai.vector.pojo;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.HashMap;
import java.util.Map;
@Data
@EqualsAndHashCode
public class QueryCondition {
    private String text;
    private Integer n;
    private Map<String, String> where = new HashMap<>();
    private Double similarityCutoff;

}

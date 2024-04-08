package ai.vector.pojo;

import java.util.HashMap;
import java.util.Map;

public class QueryCondition {
    private String text;
    private Integer n;
    private Map<String, String> where = new HashMap<>();

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Integer getN() {
        return n;
    }

    public void setN(Integer n) {
        this.n = n;
    }

    public Map<String, String> getWhere() {
        return where;
    }

    public void setWhere(Map<String, String> where) {
        this.where = where;
    }
}

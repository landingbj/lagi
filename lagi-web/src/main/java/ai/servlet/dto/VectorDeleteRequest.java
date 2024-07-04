package ai.servlet.dto;


import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class VectorDeleteRequest {
    private String category;
    private List<String> ids;
    private List<Map<String, String>> whereList;
}

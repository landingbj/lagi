package ai.workflow.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskRunInput {
    private String schema;
    private Map<String, Object> inputs;
} 
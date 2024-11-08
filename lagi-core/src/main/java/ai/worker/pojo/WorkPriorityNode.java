package ai.worker.pojo;

import lombok.Data;

import java.util.List;

@Data
public class WorkPriorityNode {
    private String name;
    private List<String> words;
}

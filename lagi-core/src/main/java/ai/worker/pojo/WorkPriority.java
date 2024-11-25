package ai.worker.pojo;

import lombok.Data;

import java.util.List;

@Data
public class WorkPriority {
    List<WorkPriorityNode> works;
}

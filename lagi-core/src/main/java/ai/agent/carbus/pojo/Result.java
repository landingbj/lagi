package ai.agent.carbus.pojo;


import lombok.*;

import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Result<T> {
    private String event;
    private String session_id;
    private String step_id;
    private Long created;
    private T data;
    private List<String> url_list;
    private List<String> search_results;
    private String node_id;
    private String record_id;
    private String task_name;
    private String node_name;
    private String reset_name;
    private Map<String, Object> usage;
    private Object model_stop_reason;
}

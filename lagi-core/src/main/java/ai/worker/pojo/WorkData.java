package ai.worker.pojo;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class WorkData<T> {
    private String agentId;
    private T data;
}

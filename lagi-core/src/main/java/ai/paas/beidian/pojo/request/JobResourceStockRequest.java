package ai.paas.beidian.pojo.request;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class JobResourceStockRequest {
    private int mainResourceLimit;
    private int currentRoleIndex;
    private List<TaskRole> taskRoleList;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @ToString
    public static class TaskRole {
        private int index;
        private int instanceCount;
        private int resourceType;
        private String GPU;
    }
}

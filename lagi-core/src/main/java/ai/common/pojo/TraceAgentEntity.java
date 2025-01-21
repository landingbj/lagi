package ai.common.pojo;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TraceAgentEntity {
    private Integer id;
    private String name;
    private Integer agentId;
    private Integer count;
}

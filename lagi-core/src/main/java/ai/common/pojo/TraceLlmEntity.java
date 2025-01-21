package ai.common.pojo;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TraceLlmEntity {
    private Integer id;
    private String name;
    private Integer count;
}

package ai.vector.pojo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class VectorCollection {
    private String category;
    private int vectorCount;
}

package ai.common.pojo;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class ImageEnhanceResult {
    private String type;
    private String data;
}

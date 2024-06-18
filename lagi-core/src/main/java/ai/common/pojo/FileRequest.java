package ai.common.pojo;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.util.Map;

@Builder
@ToString
@Data
public class FileRequest {
    String model;
    String imageUrl;
    Map<String, Object> extendParam;
}

package ai.paas.beidian.pojo.request;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class UpdateImageBaseInfoRequest {
    private Integer imageId;
    private String name;
    private String description;
}

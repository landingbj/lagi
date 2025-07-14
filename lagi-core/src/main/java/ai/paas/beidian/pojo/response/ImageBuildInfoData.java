package ai.paas.beidian.pojo.response;

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
public class ImageBuildInfoData {
    private Integer imageId;
    private Integer jobEnvId;
    private String name;
    private String createUserDisplayName;
    private String status;
    private String statusName;
    private String errMsg;
}

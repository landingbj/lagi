package ai.paas.beidian.pojo;

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
public class AZInfo {
    private String clusterId;
    private String clusterName;
    private String azId;
    private String azName;
    private String azDisplayName;
    private String azShowColor;
}

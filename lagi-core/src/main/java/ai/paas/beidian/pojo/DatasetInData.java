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
public class DatasetInData {
    private int dataType;
    private String dataId;
    private String dataPath;
    private String dataBucket;
    private String version;
    private String versionId;
}

package ai.paas.beidian.pojo;

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
public class InData {
    private int dataType;
    private String dataId;
    private String dataDesc;
    private String dataPath;
    private String dataBucket;
    private int dataSize;
    private String usePath;
    private String envName;
    private String version;
    private String versionId;
    private List<AZInfo> azList;
}

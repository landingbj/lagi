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
    private boolean isDataDeleted;
    private boolean isDataUsable;
    private boolean isDataVersionDeleted;
    private int dataType;
    private String dataId;
    private String dataName;
    private String dataDesc;
    private String dataPath;
    private String dataBucket;
    private long dataSize;
    private Integer sizeSync;
    private String usePath;
    private String envName;
    private String version;
    private String versionId;
    private String lastVersionId;
    private List<AZInfo> azList;
    private String modelId;
    private String modelName;
    private String userPath;
    private Integer permissions;
    private String coverPagePath;
    private String fileName;
    private String spaceId;
    private String spaceName;
    private String storageName;
    private Integer accessType;
    private String accessTypeName;
    private Long createUserId;
    private String createDisplayName;
    private String createUserEmail;
    private Long createTime;
    private Long updateTime;
    private String modelType;
    private String outputJobId;
    private Boolean outputJobIsAuto;
}

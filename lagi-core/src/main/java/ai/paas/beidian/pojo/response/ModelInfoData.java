package ai.paas.beidian.pojo.response;

import ai.paas.beidian.pojo.*;
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
public class ModelInfoData {
    private String modelId;
    private String modelName;
    private String description;
    private String spaceId;
    private int accessType;
    private String modelType;
    private int source;
    private long modelSize;
    private int createUserId;
    private String createDisplayName;
    private String createUserEmail;
    private long createTime;
    private long updateTime;
    private int pavoStatus;
    private String coverPagePath;
    private String version;
    private String versionId;
    private String status;

    private List<Label> labels;

    private int sizeSync;
    private String spaceName;
    private String updateDisplayName;
    private int permissions;

    private List<ShareSpace> shareSpaces;

    private int channelId;
    private int sftpPodType;
    private boolean isUnzip;
    private String unzipPath;
    private String unzipFile;
    private String versionCount;

    private DraftVersionInfo draftVersionInfo;
    private VersionInfo lastVersionInfo;
    private CloneInfo cloneInfo;
}

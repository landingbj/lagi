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
public class VersionInfo {
    private String version;
    private String versionId;
    private String baseVersionId;
    private String baseVersion;
    private String createTime;
    private Integer createUserId;
    private String createUserDisplayName;
    private Integer ownerRelateJobCnt;
    private Integer nonOwnerRelateJobCnt;
    private String pavoStatus;
    private String logicalSize;
    private String realSize;

    private PublishDataInfo publishDataInfo;
}

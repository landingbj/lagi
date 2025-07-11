package ai.paas.beidian.pojo.request;

import ai.paas.beidian.pojo.AptAddPkg;
import ai.paas.beidian.pojo.ImageFileInfo;
import ai.paas.beidian.pojo.PipAddPkg;
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
public class ImageBuildRequest {
    private String name;
    private Integer fromImageId;
    private String description;
    private String dockerFileText;
    private List<AptAddPkg> aptAddPkgList;
    private List<PipAddPkg> pipAddPkgList;
    private Integer jobEnvId;
    private String jobId;
    private Integer imageId;
    private List<ImageFileInfo> imageFileInfoList;
}

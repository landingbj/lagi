package ai.paas.beidian.pojo.response;

import ai.paas.beidian.pojo.AptAddPkg;
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
public class ImageInstallPkgData {
    private AptAddPkg aptAddPkgList;
    private PipAddPkg pipAddPkgList;
    private List<AptAddPkg> aptAllPkgList;
    private List<PipAddPkg> pipAllPkgList;
}

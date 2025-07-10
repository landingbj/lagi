package ai.paas.beidian.pojo.request;

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
public class IntelligentGenRequest {
    private List<AptAddPkg> aptAddPkgList;
    private List<PipAddPkg> pipAddPkgList;
}

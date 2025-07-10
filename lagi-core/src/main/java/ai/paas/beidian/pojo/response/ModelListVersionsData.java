package ai.paas.beidian.pojo.response;

import ai.paas.beidian.pojo.DraftVersionInfo;
import ai.paas.beidian.pojo.VersionInfo;
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
public class ModelListVersionsData {
    private List<VersionInfo> versionList;
    private DraftVersionInfo draft;
    private String modelId;
    private String modelName;
}

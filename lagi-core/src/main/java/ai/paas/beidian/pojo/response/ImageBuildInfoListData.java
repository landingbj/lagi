package ai.paas.beidian.pojo.response;

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
public class ImageBuildInfoListData {
    private int totalCount;
    private List<ImageBuildInfoData> imageBuildInfoList;
    private int statusBuildingCount;
    private int statusBuildSuccessCount;
    private int statusFailSuccessCount;
    private int statusAllCount;
}

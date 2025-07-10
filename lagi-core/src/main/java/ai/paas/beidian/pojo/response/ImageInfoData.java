package ai.paas.beidian.pojo.response;

import ai.paas.beidian.pojo.CloneInfo;
import ai.paas.beidian.pojo.ImageFileInfo;
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
public class ImageInfoData {
    private Integer imageId;
    private Integer imageRepositoryId;
    private String name;
    private String description;

    private ImageRepositoryData fromImageInfo;

    private Integer dockerFileText;
    private String createUserDisplayName;
    private String createUserEmail;
    private Integer layers;
    private Long createTime;
    private Long updateTime;
    private Integer buildType;

    private List<ImageFileInfo> imageFileInfoList;

    private CloneInfo cloneInfo;
}

package ai.common.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class UserRagSetting {
    private Integer id;
    private String userId;
    private String fileType;
    private String category;
    private Integer chunkSize;
    private Double temperature;
}

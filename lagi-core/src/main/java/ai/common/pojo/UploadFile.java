package ai.common.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class UploadFile {
    private String fileId;
    private String filename;
    private String filepath;
    private String category;
    private Long createTime;
    private String userId;

}

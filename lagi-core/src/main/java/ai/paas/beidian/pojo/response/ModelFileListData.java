package ai.paas.beidian.pojo.response;

import ai.paas.beidian.pojo.FileItem;
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
public class ModelFileListData {
    private int listCount;
    private List<FileItem> fileList;
}

package ai.common.pojo;

import lombok.Data;
import lombok.ToString;

import java.util.List;


@ToString
@Data
public class IndexSearchData {
    private String id;
    private String text;
    private String category;
    private String fileId;
    private List<String> filename;
    private List<String> filepath;
    private Float distance;
    private String image;
    private List<String> imageList;
    private String level;
    private String parentId;

}

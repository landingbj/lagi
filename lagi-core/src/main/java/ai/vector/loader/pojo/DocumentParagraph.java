package ai.vector.loader.pojo;

import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class DocumentParagraph {

    /**
     *  txt image table video ...
     */
    private String type;

    /**
     * heading 1 ....
     */
    private String subType;

    /**
     * 1 2 3 4 5 6
     */
    private String level;

    private String txt;

    private List<String> images;

    private List<List<String>> table;

}

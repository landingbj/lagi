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

    private String txt;

    private List<String> images;

    private List<List<String>> table;

}

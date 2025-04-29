package ai.vector.loader.pojo;

import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class Document {

    /**
     * 0 txt 1 doc 2 docx 3 ppt 4 pptx 5 pdf 6 csv 7 excel 8 html 9 markdown 10 image
     */
    private Integer type;

    private List <DocumentParagraph> paragraphs;

    private String fileName;

    private Integer titleCount;


}

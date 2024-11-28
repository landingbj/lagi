package ai.dto;

import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class PdfSearchRequest {
    private String pdfPath;
    private String fileName;
    private List<String> searchWords;
    private Integer extend;
}

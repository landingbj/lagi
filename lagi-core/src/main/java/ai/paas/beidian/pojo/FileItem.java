package ai.paas.beidian.pojo;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class FileItem {
    private String name;
    private String type;
    private String size;
    private String lastModify;
    private String storage;
}

package ai.image.pojo;

import lombok.*;

@ToString
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SparkGenImgHeader {
    private String app_id;
    private Integer code;
    private String message;
    private String sid;
    private Integer status;
}

package ai.audio.pojo;

import lombok.*;


@Data
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class VolcRequest {
    private String reqid;
    private String text;
    private String text_type = "plain";
    private String operation = "query";
}

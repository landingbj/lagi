package ai.audio.pojo;

import lombok.*;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VolcBaseResp {
    private Integer StatusCode;
    private String StatusMessage;
}

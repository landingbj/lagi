package ai.audio.pojo;

import lombok.*;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VolcTtsResponse {
    /**
     *  doc url : https://www.volcengine.com/docs/6561/79823
     */
    private String reqid;
    private Integer code;
    private String message;
    private Integer sequence;
    private String data;
    private String operation;
    private Addition addition;

    @Data
    @ToString
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    static class Addition{
        private String duration;
        private String frontend;
    }
}

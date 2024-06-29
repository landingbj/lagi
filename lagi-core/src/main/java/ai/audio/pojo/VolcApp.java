package ai.audio.pojo;

import lombok.*;

@Data
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class VolcApp {
    private String appid;
    private String token ; // 目前未生效，填写默认值：access_token
    private String cluster;
}

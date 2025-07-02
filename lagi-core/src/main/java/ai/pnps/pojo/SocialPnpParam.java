package ai.pnps.pojo;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
public class SocialPnpParam extends PnpParam {
    private String appId;
    private String username;
    private String robotFlag;
    private String timerFlag;
    private String repeaterFlag;
    private String guideFlag;
}

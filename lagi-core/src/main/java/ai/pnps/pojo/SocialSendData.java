package ai.pnps.pojo;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class SocialSendData extends PnpData {
    private String channelUser;
    private String text;
}

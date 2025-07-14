package ai.pnps.pojo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RpaContact {
    private Integer id;
    private Integer channelId;
    private String contactName;
    private Integer appId;
}

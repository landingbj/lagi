package ai.pnps.pojo;

import lombok.Builder;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
@Builder
public class RpaTimerTask {
    private Integer appId;
    private Date sendTime;
    private List<Integer> contactIdList;
    private String message;
    private Integer channelId;
    private Integer repeatFlag;
    private Integer repeatDay;
    private Integer repeatHour;
    private Integer repeatMinute;
}

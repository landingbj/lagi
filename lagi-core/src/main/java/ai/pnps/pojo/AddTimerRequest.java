package ai.pnps.pojo;

import lombok.Data;

import java.util.Date;

@Data
public class AddTimerRequest {
    private Integer appId;
    private Integer channelId;
    private Date sendTime;
    private String contact;
    private String message;
}

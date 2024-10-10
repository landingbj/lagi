package ai.worker.pojo;

import lombok.Data;

@Data
public class MeetingInfo {
    private String meetingAddress;
    private String date;
    private String startTime;
    private String duration;
    private String attendance;
}

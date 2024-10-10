package ai.worker.pojo;

import lombok.Data;

@Data
public class AddMeetingRequest {
    private String message;
    private MeetingInfo meetingInfo;
}

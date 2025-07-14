package ai.pnps.pojo;

import lombok.Data;

import java.util.List;

@Data
public class StartRobotRequest {
    private String prompt;
    private List<String> appIdList;
    private String username;
}

package ai.servlet.dto;

import lombok.Data;

@Data
public class LoginResponse {
    private String status;
    private Data data;
    private String msg;

    @lombok.Data
    public static class Data {
        private String channelId;
        private String channelName;
        private String logo;
        private String username;
        private int role;
        private String userId;
    }
}

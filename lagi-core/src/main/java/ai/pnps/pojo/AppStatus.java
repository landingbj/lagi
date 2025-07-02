package ai.pnps.pojo;

import lombok.Data;

@Data
public class AppStatus {
    private Integer appId;
    private String appName;
    private String appIcon;
    private String appPackage;
    private String appClass;
    private String iosAppUrl;
    private String loginMethod;
    private Integer selectedStatus;
    private Integer authorizedStatus;
    private Integer channelId;
    private String username;
    private String funcFlag;
}

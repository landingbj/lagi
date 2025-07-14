package ai.paas.beidian.pojo.response;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class SftpInfoData {
    private int channelId;
    private String sftpIp;
    private int sftpPort;
    private String sftpUser;
    private String sftpPassword;
    private int sftpPodType;
    private long openTime;
}

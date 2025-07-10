package ai.paas.beidian.pojo;

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
public class DataPrepareProcessInfo {
    private String dataDisplayName;
    private String process;
    private long prepareEndTime;
    private long prepareStartTime;
}

package ai.pnps.pojo;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class SocialReceiveData extends PnpData {
    private String status;
    private String data;
}

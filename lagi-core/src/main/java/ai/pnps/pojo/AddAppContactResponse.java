package ai.pnps.pojo;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AddAppContactResponse {
    private Integer existFlag;
    private String status;
    private List<RpaContact> data;
}

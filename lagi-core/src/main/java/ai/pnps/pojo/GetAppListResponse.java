package ai.pnps.pojo;

import lombok.Data;

import java.util.List;

@Data
public class GetAppListResponse {
    private String status;
    private List<AppStatus> data;
}

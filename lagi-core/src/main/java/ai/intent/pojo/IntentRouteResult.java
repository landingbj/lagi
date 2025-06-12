package ai.intent.pojo;

import lombok.Data;

import java.util.List;

@Data
public class IntentRouteResult {
    private String modal;
    private String status;
    private Integer continuedIndex;
    private List<Integer> agents;
    private Boolean firstStream;
    private Boolean allSolid;
    private String invoke;
}

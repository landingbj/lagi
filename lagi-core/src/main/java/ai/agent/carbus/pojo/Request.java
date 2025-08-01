package ai.agent.carbus.pojo;


import lombok.*;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Request {
    private List<String> doc_list;
    private String image_url;
    private String query;
    private String session_id;
    private Boolean stream;
    private Double longitude;
    private Double latitude;
    private String type;
}

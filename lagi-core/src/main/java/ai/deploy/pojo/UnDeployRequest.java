package ai.deploy.pojo;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class UnDeployRequest {
    private Integer id;
    private String userId;
    private String port;
}

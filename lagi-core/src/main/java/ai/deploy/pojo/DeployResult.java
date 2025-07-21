package ai.deploy.pojo;


import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class DeployResult {
    private int code;
    private String message;
    private String apiAddress;
}

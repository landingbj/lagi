package ai.deploy.pojo;


import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class UnDeployResult {
    private int code;
    private String message;
    private String apiAddress;
}

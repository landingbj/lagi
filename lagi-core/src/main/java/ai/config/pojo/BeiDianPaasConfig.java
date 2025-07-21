package ai.config.pojo;


import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class BeiDianPaasConfig {
    private String account;
    private String password;
    private String baseUrl;
    private String spaceId;
    private String acceptLanguage;

    // optional
    private String projectId;
    private String inferenceId;
    private Long imageId;
}

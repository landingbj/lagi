package ai.common.pojo;

import lombok.*;


@ToString
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ImageGenerationRequest {
    private String prompt;
    private String negative_prompt;
    private String model;
    private Integer n;
    private String quality;
    private String response_format;
    private String size;
    private String style;
    private String user;
    private Integer step;
    private String sampler_index;
    private String user_id;
    private Integer seed;

}

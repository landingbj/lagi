package ai.worker.pojo;

import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class IntentResponse {
    private String intent;
    private List<String> keywords;
}
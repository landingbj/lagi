package ai.dto;

import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class UserDatasetRequest {
    private String userId;
    private List<String> datasetNames;
}

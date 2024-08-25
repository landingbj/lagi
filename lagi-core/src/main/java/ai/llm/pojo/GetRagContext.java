package ai.llm.pojo;

import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
@Data
public class GetRagContext {
    private String context;
    private List<String> filePaths;
    private List<String> filenames;
}

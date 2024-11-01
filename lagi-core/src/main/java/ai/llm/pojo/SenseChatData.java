package ai.llm.pojo;

import lombok.*;

import java.util.List;
import java.util.Map;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SenseChatData {
    private String id;
    private SenseChatUsage usage;
    private List<SenseChatChoice> choices;
    private Map<String, Object> plugins;
}

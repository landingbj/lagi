package ai.llm.pojo;

import lombok.*;

import java.util.List;
import java.util.Map;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SenseChatChoice {
    private Integer index;
    private String role;
    private String message;
    private String finish_reason;
}

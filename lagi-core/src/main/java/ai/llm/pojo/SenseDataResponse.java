package ai.llm.pojo;

import lombok.*;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SenseDataResponse {
    private SenseChatData data;
}

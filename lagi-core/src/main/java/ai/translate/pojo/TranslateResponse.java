package ai.translate.pojo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class TranslateResponse {
    private String from;
    private String to;
    List<TranslateResult> transResult;
}

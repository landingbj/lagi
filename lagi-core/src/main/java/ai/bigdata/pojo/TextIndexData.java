package ai.bigdata.pojo;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@Data
public class TextIndexData {
    private String id;
    private String text;
    private String category;
}

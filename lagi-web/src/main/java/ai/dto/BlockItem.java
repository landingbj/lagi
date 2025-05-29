package ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class BlockItem {
    @JsonProperty("blue_marked_number")
    private String blueMarkedNumber;
    @JsonProperty("largest_font_text")
    private String largestFontText;
}

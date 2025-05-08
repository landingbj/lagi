package ai.medusa.pojo;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@EqualsAndHashCode(exclude = {"medusaMetadata"})
@ToString
public class PromptInput implements Serializable {
    private List<String> promptList;
    private PromptParameter parameter;
    private MedusaMetadata medusaMetadata;
}

package ai.servlet.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import java.sql.Timestamp;

@Data
@EqualsAndHashCode
public class KnowledgeBase {
    private Long id;
    private String userId;
    private String name;
    private String description;
    private String category;
    private boolean isPublic;
    private boolean isActive;
    private boolean isDefault;

}

package ai.dto;

import lombok.Data;

@Data
public class BlockDesc {
    private String id;
    private String block;
    private String description;
    private Rectangle rectangle;
}

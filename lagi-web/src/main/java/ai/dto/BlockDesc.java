package ai.dto;

import lombok.Data;

@Data
public class BlockDesc {
    private Integer id;
    private String block;
    private String description;
    private Rectangle rectangle;
}

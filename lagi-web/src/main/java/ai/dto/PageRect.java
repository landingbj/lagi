package ai.dto;

import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class PageRect {
    private Integer page;
    List<Integer> rect;
}

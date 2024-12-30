package ai.router.pojo;

import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class Parser {
    private String name;
    private List<String> args;
}

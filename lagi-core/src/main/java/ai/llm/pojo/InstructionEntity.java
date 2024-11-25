package ai.llm.pojo;

import lombok.Data;

@Data
public class InstructionEntity {
    private String instruction;
    private String input;
    private String output;
}

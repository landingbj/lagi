package ai.migrate.pojo;

import java.util.Objects;

public class InstructionsResponse {
    private String instruction;
    private String input;
    private String output;

    @Override
    public String toString() {
        return "InstructionsResponse{" +
                "instruction='" + instruction + '\'' +
                ", input='" + input + '\'' +
                ", output='" + output + '\'' +
                '}';
    }

    public String getInstruction() {
        return instruction;
    }

    public void setInstruction(String instruction) {
        this.instruction = instruction;
    }

    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }

    public String getOutput() {
        return output;
    }

    public void setOutput(String output) {
        this.output = output;
    }

    public InstructionsResponse(String instruction, String input, String output) {
        this.instruction = instruction;
        this.input = input;
        this.output = output;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InstructionsResponse that = (InstructionsResponse) o;
        return Objects.equals(instruction, that.instruction) && Objects.equals(input, that.input) && Objects.equals(output, that.output);
    }

    @Override
    public int hashCode() {
        return Objects.hash(instruction, input, output);
    }
}

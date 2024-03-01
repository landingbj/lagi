package ai.migrate.pojo;

public class Instruction {
    private String instruction;
    private String input;
    private String output;

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

    @Override
    public String toString() {
        return "Instruction [instruction=" + instruction + ", input=" + input + ", output=" + output + "]";
    }
}

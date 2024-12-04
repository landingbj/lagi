package ai.medusa.pojo;

import lombok.Data;

@Data
public class InstructionPairRequestContractedHotels extends InstructionPairRequest{
    private String place;
    private String type;
}

package ai.medusa.pojo;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import lombok.Data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Data
public class InstructionPairRequest {
    private String category;
    private String level;
    @JsonDeserialize(using = InstructionListDeserializer.class)
    private List<InstructionData> data;


    public static class InstructionListDeserializer extends StdDeserializer<List<InstructionData>> {
        public InstructionListDeserializer() {
            this(null);
        }

        public InstructionListDeserializer(Class<?> vc) {
            super(vc);
        }

        @Override
        public List<InstructionData> deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
            JsonNode node = jp.getCodec().readTree(jp);
            ObjectMapper mapper = (ObjectMapper) jp.getCodec();
            List<InstructionData> instructions = new ArrayList<>();

            if (node.isArray()) {
                for (JsonNode elementNode : node) {
                    instructions.add(mapper.treeToValue(elementNode, InstructionData.class));
                }
            } else if (node.isObject()) {
                instructions.add(mapper.treeToValue(node, InstructionData.class));
            }

            return instructions;
        }
    }
}

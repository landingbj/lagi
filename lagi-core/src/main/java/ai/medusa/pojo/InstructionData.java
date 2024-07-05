package ai.medusa.pojo;

import com.fasterxml.jackson.core.JsonParser;
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
public class InstructionData {
    @JsonDeserialize(using = InstructionListDeserializer.class)
    private List<String> instruction;
    private String output;

    public static class InstructionListDeserializer extends StdDeserializer<List<String>> {
        public InstructionListDeserializer() {
            this(null);
        }

        public InstructionListDeserializer(Class<?> vc) {
            super(vc);
        }

        @Override
        public List<String> deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
            JsonNode node = jp.getCodec().readTree(jp);
            ObjectMapper mapper = (ObjectMapper) jp.getCodec();
            List<String> instructions = new ArrayList<>();
            if (node.isArray()) {
                for (JsonNode elementNode : node) {
                    instructions.add(mapper.treeToValue(elementNode, String.class));
                }
            } else if (node.isTextual()) {
                instructions.add(mapper.treeToValue(node, String.class));
            }
            return instructions;
        }
    }
}

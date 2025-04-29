package ai.openai.serializer;

import ai.openai.pojo.MultiModalContent;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.util.List;

public class ContentSerializer extends JsonSerializer<String> {
    private final ObjectMapper mapper;

    public ContentSerializer() {
        this.mapper = new ObjectMapper();
    }

    @Override
    public void serialize(String value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        try {
            List<MultiModalContent> contentList = mapper.readValue(value, new TypeReference<List<MultiModalContent>>() {});
            gen.writeObject(contentList);
        } catch (Exception e) {
            gen.writeString(value);
        }
    }
}

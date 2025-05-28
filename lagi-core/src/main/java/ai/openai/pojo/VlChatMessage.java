package ai.openai.pojo;

import ai.openai.deserializer.ContentDeserializer;
import ai.openai.serializer.ContentSerializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.*;

import java.io.Serializable;
import java.util.List;

@Data
@EqualsAndHashCode
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VlChatMessage implements Serializable {
    private String role;
    private List<VlChatContent> content;
}

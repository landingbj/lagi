package ai.openai.pojo;

import ai.openai.deserializer.ContentDeserializer;
import ai.openai.serializer.ContentSerializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.*;

import java.util.List;

@Data
@EqualsAndHashCode
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessage {
    private String role;
    @JsonSerialize(using = ContentSerializer.class)
    @JsonDeserialize(using = ContentDeserializer.class)
    private String content;
    private String reasoning_content;
    private List<String> filename;
    private List<String> filepath;
    private String author;
    private Float distance;
    private String image;
    private List<String> imageList;
    private String context;
    private List<String> contextChunkIds;
    private List<ToolCall> tool_calls;
    private String tool_call_id;
}

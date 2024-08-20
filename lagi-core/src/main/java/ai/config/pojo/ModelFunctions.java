package ai.config.pojo;

import ai.common.pojo.Backend;
import ai.common.pojo.EmbeddingConfig;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.ToString;

import java.util.List;

@ToString
@Data
public class ModelFunctions {
    @JsonProperty("chat_policy")
    private String chatPolicy;
    private List<EmbeddingConfig> embedding;
    @JsonProperty("chat")
    private List<Backend> chat;
    @JsonProperty("speech2text")
    private List<Backend> speech2text;
    @JsonProperty("text2speech")
    private List<Backend> text2speech;
    @JsonProperty("text2image")
    private List<Backend> text2image;
    @JsonProperty("image2text")
    private List<Backend> image2text;
    @JsonProperty("image2enhance")
    private List<Backend> image2Enhance;
    @JsonProperty("text2video")
    private List<Backend> text2video;
    @JsonProperty("image2video")
    private List<Backend> image2video;
    @JsonProperty("video2track")
    private List<Backend> video2Track;
    @JsonProperty("video2enhance")
    private List<Backend> video2Enhance;
    @JsonProperty("translate")
    private List<Backend> translate;
    @JsonProperty("speech2clone")
    private List<Backend> speech2clone;
    @JsonProperty("image2ocr")
    private List<Backend> image2ocr;
}

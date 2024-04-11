package ai.config.pojo;

import ai.common.pojo.Backend;
import ai.common.pojo.EmbeddingConfig;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.ToString;

@ToString
@Data
public class ModelFunctions {

    private EmbeddingConfig embedding;
    private String streamBackend;
    @JsonProperty("RAG")
    private Backend RAG;
    @JsonProperty("ASR")
    private ModelFunction ASR;
    @JsonProperty("TTS")
    private ModelFunction TTS;
    @JsonProperty("image2generation")
    private ModelFunction image2Generation;
    @JsonProperty("image2captioning")
    private ModelFunction image2Captioning;
    @JsonProperty("image2enhance")
    private ModelFunction image2Enhance;
    @JsonProperty("video2generation")
    private ModelFunction video2Generation;
    @JsonProperty("video2track")
    private ModelFunction video2Track;
    @JsonProperty("video2enhance")
    private ModelFunction video2Enhance;

}

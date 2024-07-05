package ai.common.pojo;

import ai.config.pojo.AgentConfig;
import ai.config.pojo.WorkerConfig;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
@ToString
@Builder
public class Configuration {
    private String systemTitle;
    @JsonProperty("LLM")
    private LLM LLM;
    private List<VectorStoreConfig> vectorStores;
    @JsonProperty("ASR")
    private ASR ASR;
    @JsonProperty("TTS")
    private TTS TTS;
    private ImageGeneration imageGeneration;
    private ImageCaptioning imageCaptioning;
    private ImageEnhance imageEnhance;
    private VideoGeneration videoGeneration;
    private VideoTrack videoTrack;
    private VideoEnhance videoEnhance;
    private List<AgentConfig> agents;
    private List<WorkerConfig> workers;
}

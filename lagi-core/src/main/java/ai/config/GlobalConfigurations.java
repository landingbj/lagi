package ai.config;

import ai.common.pojo.*;
import ai.config.pojo.AgentConfig;
import ai.config.pojo.ModelFunctions;
import ai.config.pojo.WorkerConfig;
import ai.managers.*;
import ai.utils.LagiGlobal;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

@EqualsAndHashCode(callSuper = true)
@ToString
@Data
public class GlobalConfigurations extends AbstractConfiguration {
    private Logger logger = LoggerFactory.getLogger(GlobalConfigurations.class);

    private String systemTitle;
    private List<Backend> models;
    private List<VectorStoreConfig> vectors;
    private ModelFunctions functions;
    private List<AgentConfig> agents;
    private List<WorkerConfig> workers;


    private void init() {
        LlmManager.getInstance().register(models, functions.getChat());
        VectorStoreManager.getInstance().register(vectors, functions.getRAG(), functions.getEmbedding());
        ASRManager.getInstance().register(models, functions.getSpeech2text());
        TTSManager.getInstance().register(models, functions.getText2speech());
        Image2TextManger.getInstance().register(models, functions.getImage2text());
        ImageGenerationManager.getInstance().register(models, functions.getText2image());
        ImageEnhanceManager.getInstance().register(models, functions.getImage2Enhance());
        Text2VideoManager.getInstance().register(models, functions.getText2video());
        Image2VideoManager.getInstance().register(models, functions.getImage2video());
        Video2EnhanceManger.getInstance().register(models, functions.getVideo2Enhance());
        Video2TrackManager.getInstance().register(models, functions.getVideo2Track());
    }




    @Override
    public Configuration transformToConfiguration() {
        init();
        List<Backend> chatBackends = functions.getChat().stream().map(backendMatch -> {
            Optional<Backend> any = models.stream().filter(backend -> backend.getEnable() && backendMatch.getEnable() && backendMatch.getBackend().equals(backend.getName())).findAny();
            Backend backend = any.orElse(null);
            if(backend != null) {
                backend.setPriority(backendMatch.getPriority());
            }
            return backend;
        }).filter(Objects::nonNull).collect(Collectors.toList());
        LLM llm = LLM.builder().backends(models).embedding(functions.getEmbedding().get(0))
                .streamBackend(functions.getStreamBackend())
                .chatBackends(chatBackends)
                .build();
        llm.getBackends().forEach(backend -> {
            if (backend.getPriority() == null) {
                backend.setPriority(10);
            }
        });

        return Configuration.builder()
                .systemTitle(systemTitle)
                .vectorStores(vectors)
                .LLM(llm)
                .ASR(ASR.builder().backends(functions.getSpeech2text()).build())
                .TTS(TTS.builder().backends(functions.getText2speech()).build())
                .imageEnhance(ImageEnhance.builder().backends(functions.getImage2Enhance()).build())
                .imageGeneration(ImageGeneration.builder().backends(functions.getText2image()).build())
                .imageCaptioning(ImageCaptioning.builder().backends(functions.getImage2text()).build())
                .videoEnhance(VideoEnhance.builder().backends(functions.getVideo2Enhance()).build())
                .videoGeneration(VideoGeneration.builder().backends(functions.getImage2video()).build())
                .videoTrack(VideoTrack.builder().backends(functions.getVideo2Track()).build())
                .agents(agents)
                .workers(workers)
                .build();
    }

    public static void main(String[] args) throws IOException {
        File file = new File("C:\\lz\\work\\lagi\\lagi-web\\src\\main\\resources\\lagi.yml");
        InputStream inputStream = Files.newInputStream(file.toPath());
        GlobalConfigurations globalConfigurations = (GlobalConfigurations) LagiGlobal.loadConfig(inputStream, GlobalConfigurations.class);
        System.out.println(globalConfigurations);
        Configuration config = LagiGlobal.getConfig();
        System.out.println(config);

    }
}

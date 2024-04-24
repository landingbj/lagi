package ai.config;

import ai.common.pojo.*;
import ai.config.pojo.ModelFunctions;
import ai.utils.LagiGlobal;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.*;
import java.nio.file.Files;
import java.util.List;
import java.util.Objects;

@EqualsAndHashCode(callSuper = true)
@ToString
@Data
public class GlobalConfigurations extends AbstractConfiguration{
    private String systemTitle;
    private List<Backend>  models;
    private List<VectorStoreConfig> vectors;
    private ModelFunctions functions;


    @Override
    public Configuration transformToConfiguration() {
        LLM llm = LLM.builder().backends(models).embedding(functions.getEmbedding()).streamBackend(functions.getStreamBackend()).build();
        llm.getBackends().forEach(backend -> {
            if(backend.getPriority() == null) {
                backend.setPriority(10);
            }
        });
        VectorStoreConfig vectorStoreConfig = vectors.stream().filter(vc -> Objects.equals(vc.getName(), functions.getRAG().getName())).findAny().orElse(null);

        if(vectorStoreConfig != null ) {
            if(vectorStoreConfig.getType() == null) {
                vectorStoreConfig.setType(vectorStoreConfig.getName());
            }
            vectorStoreConfig.setSimilarityTopK(functions.getRAG().getSimilarityTopK());
            vectorStoreConfig.setSimilarityCutoff(functions.getRAG().getSimilarityCutoff());
            vectorStoreConfig.setParentDepth(functions.getRAG().getParentDepth());
            vectorStoreConfig.setChildDepth(functions.getRAG().getChildDepth());
        }
        return Configuration.builder()
                .systemTitle(systemTitle)
                .vectorStore(vectorStoreConfig)
                .LLM(llm)
                .ASR(ASR.builder().backends(functions.getASR().getBackends()).build())
                .TTS(TTS.builder().backends(functions.getTTS().getBackends()).build())
                .imageEnhance(ImageEnhance.builder().backends(functions.getImage2Enhance().getBackends()).build())
                .imageGeneration(ImageGeneration.builder().backends(functions.getImage2Generation().getBackends()).build())
                .imageCaptioning(ImageCaptioning.builder().backends(functions.getImage2Captioning().getBackends()).build())
                .videoEnhance(VideoEnhance.builder().backends(functions.getVideo2Enhance().getBackends()).build())
                .videoGeneration(VideoGeneration.builder().backends(functions.getVideo2Generation().getBackends()).build())
                .videoTrack(VideoTrack.builder().backends(functions.getVideo2Track().getBackends()).build())
                .build();
    }

    public static void main(String[] args) throws IOException {
        File file = new File("C:\\lz\\work\\lagi\\lagi-web\\src\\main\\resources\\lagi.yml");
        InputStream inputStream = Files.newInputStream(file.toPath());
        GlobalConfigurations globalConfigurations = (GlobalConfigurations)LagiGlobal.loadConfig(inputStream, GlobalConfigurations.class);
        System.out.println(globalConfigurations);
        Configuration config = LagiGlobal.getConfig();
        System.out.println(config);
    }
}

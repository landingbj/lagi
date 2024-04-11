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
        VectorStoreConfig vectorStoreConfig = vectors.get(0);
        if(vectorStoreConfig.getType() == null) {
            vectorStoreConfig.setType(vectorStoreConfig.getName());
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
        File file = new File("C:\\lz\\work\\lagi\\lagi-web\\src\\main\\resources\\lagi_dev.yml");
        InputStream inputStream = Files.newInputStream(file.toPath());
        AbstractConfiguration abstractConfiguration = LagiGlobal.loadConfig(inputStream, GlobalConfigurations.class);
        System.out.println(abstractConfiguration);
        Configuration config = LagiGlobal.getConfig();
        System.out.println(config);
    }
}

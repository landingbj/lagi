package ai.config;

import ai.audio.AudioManager;
import ai.audio.adapter.IAudioAdapter;
import ai.common.pojo.*;
import ai.config.pojo.AgentConfig;
import ai.config.pojo.ModelFunctions;
import ai.config.pojo.WorkerConfig;
import ai.embedding.EmbeddingFactory;
import ai.embedding.Embeddings;
import ai.llm.LLMManager;
import ai.llm.adapter.ILlmAdapter;
import ai.utils.LagiGlobal;
import ai.vector.VectorStore;
import ai.vector.VectorStoreManager;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.annotation.PostConstruct;
import java.io.*;
import java.lang.reflect.Constructor;
import java.nio.file.Files;
import java.util.List;
import java.util.Objects;

@EqualsAndHashCode(callSuper = true)
@ToString
@Data
public class GlobalConfigurations extends AbstractConfiguration {
    private String systemTitle;
    private List<Backend> models;
    private List<VectorStoreConfig> vectors;
    private ModelFunctions functions;
    private List<AgentConfig> agents;
    private List<WorkerConfig> workers;

    @PostConstruct
    private void init() {
        registerVectorStore();
        registerLLM();
        registerASR();
        registerTTS();
    }

    private void registerVectorStore() {
        vectors.stream().filter((vc) -> Objects.equals(vc.getName(), functions.getRAG().getName()))
                .forEach(vectorStoreConfig -> {
                    try {
                        String name = vectorStoreConfig.getName();
                        String driver = vectorStoreConfig.getDriver();
                        Class<?> clazz = Class.forName(driver);
                        Constructor<?> constructor = clazz.getConstructor(VectorStoreConfig.class, Embeddings.class);
                        VectorStore vs = (VectorStore) constructor.newInstance(vectorStoreConfig, EmbeddingFactory.getEmbedding(functions.getEmbedding()));
                        VectorStoreManager.registerVectorStore(name, vs);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    private void registerLLM() {
        models.forEach(model -> {
            try {
                if (model.getEnable()) {
                    String name = model.getName();
                    String driver = model.getDriver();
                    Class<?> clazz = Class.forName(driver);
                    Constructor<?> constructor = clazz.getConstructor(Backend.class);
                    ILlmAdapter llmAdapter = (ILlmAdapter) constructor.newInstance(model);
                    LLMManager.registerAdapter(name, llmAdapter);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void registerASR() {
        if (functions.getASR() == null) {
            return;
        }
        List<Backend> backends = functions.getASR().getBackends();
        if (backends.isEmpty()) {
            return;
        }
        functions.getASR().getBackends().forEach(model -> {
            try {
                if (model.getEnable()) {
                    String name = model.getName();
                    String driver = model.getDriver();
                    Class<?> clazz = Class.forName(driver);
                    Constructor<?> constructor = clazz.getConstructor(Backend.class);
                    IAudioAdapter adapter = (IAudioAdapter) constructor.newInstance(model);
                    AudioManager.registerASRAdapter(name, adapter);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void registerTTS() {
        if (functions.getTTS() == null) {
            return;
        }
        List<Backend> backends = functions.getTTS().getBackends();
        if (backends.isEmpty()) {
            return;
        }
        functions.getTTS().getBackends().forEach(model -> {
            try {
                if (model.getEnable()) {
                    String name = model.getName();
                    String driver = model.getDriver();
                    Class<?> clazz = Class.forName(driver);
                    Constructor<?> constructor = clazz.getConstructor(Backend.class);
                    IAudioAdapter adapter = (IAudioAdapter) constructor.newInstance(model);
                    AudioManager.registerTTSAdapter(name, adapter);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public Configuration transformToConfiguration() {
        init();
        LLM llm = LLM.builder().backends(models).embedding(functions.getEmbedding()).streamBackend(functions.getStreamBackend()).build();
        llm.getBackends().forEach(backend -> {
            if (backend.getPriority() == null) {
                backend.setPriority(10);
            }
        });
        VectorStoreConfig vectorStoreConfig = vectors.stream().filter(vc -> Objects.equals(vc.getName(), functions.getRAG().getName())).findAny().orElse(null);

        if (vectorStoreConfig != null) {
            if (vectorStoreConfig.getType() == null) {
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
        VectorStore vectorStore = VectorStoreManager.getVectorStore();
        System.out.println(vectorStore);
    }
}

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
import ai.vector.impl.BaseVectorStore;
import cn.hutool.core.bean.BeanUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.io.*;
import java.lang.reflect.Constructor;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
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

    @PostConstruct
    private void init() {

        Map<String, Backend> modelMap = models.stream().collect(Collectors.toMap(Backend::getName, model -> model));
        Map<String, VectorStoreConfig> vectorMap = vectors.stream().collect(Collectors.toMap(VectorStoreConfig::getName, vectorStoreConfig -> vectorStoreConfig));
        registerVectorStore(vectorMap);
        registerLLM(modelMap);
        registerASR(modelMap);
        registerTTS(modelMap);
    }

    private  void  register(Map<String, Backend> modelMap, List<Backend> functions, BiConsumer<Class<?>,Backend> consumer) {
        if(functions == null) {
            return;
        }
        functions.stream().filter(Backend::getEnable).forEach(func->{
            Backend model = modelMap.get(func.getBackend());
            for (Driver driver : model.getDrivers()) {
                if(Objects.equals(driver.getModel(), func.getModel())) {
                    try {
                        Class<?> clazz = Class.forName(driver.getDriver());
                        Backend backend = new Backend();
                        BeanUtil.copyProperties(model, backend, "drivers");
                        backend.setModel(func.getModel());
                        backend.setBackend(func.getBackend());
                        backend.setStream(func.getStream());
                        consumer.accept(clazz, backend);
                    } catch (Exception e) {
                        logger.error(e.getMessage());
                    }
                }
            }
        });
    }

    private void registerVectorStore(Map<String, VectorStoreConfig> vectorMap) {
        functions.getRAG().stream().filter(Backend::getEnable)
                .map(rc->vectorMap.get(rc.getBackend()))
                .filter(Objects::nonNull)
                .forEach(vectorStoreConfig -> {
            try {
                String name = vectorStoreConfig.getName();
                String driver = vectorStoreConfig.getDriver();
                Class<?> clazz = Class.forName(driver);
                Constructor<?> constructor = clazz.getConstructor(VectorStoreConfig.class, Embeddings.class);
                BaseVectorStore vs = (BaseVectorStore) constructor.newInstance(vectorStoreConfig, EmbeddingFactory.getEmbedding(functions.getEmbedding().get(0)));
                VectorStoreManager.registerVectorStore(name, vs);
            } catch (Exception e) {
                logger.error("registerVectorStore ("+vectorStoreConfig.getName()+")error");
            }
        });
    }

    private void registerLLM(Map<String, Backend> modelMap) {
        register(modelMap, functions.getChat(), (clazz, llm) -> {
            try {
                Constructor<?> constructor = clazz.getConstructor(Backend.class);
                ILlmAdapter llmAdapter = (ILlmAdapter) constructor.newInstance(llm);
                LLMManager.registerAdapter(llm.getName(), llmAdapter);
            } catch (Exception e) {
                logger.error("registerLLM ("+llm.getName()+")error");
            }
        });
    }

    private void registerASR( Map<String, Backend> modelMap) {

        register(modelMap, functions.getSpeech2text(), (clazz, asr) -> {
            try {
                Constructor<?> constructor = clazz.getConstructor(Backend.class);
                IAudioAdapter adapter = (IAudioAdapter) constructor.newInstance(asr);
                AudioManager.registerASRAdapter(asr.getName(), adapter);
            } catch (Exception e) {
                logger.error("registerASR ("+asr.getName()+")error");
            }
        });

    }

    private void registerTTS( Map<String, Backend> modelMap) {

        register(modelMap, functions.getText2speech(), (clazz, tts) -> {
            try {
                Constructor<?> constructor = clazz.getConstructor(Backend.class);
                IAudioAdapter adapter = (IAudioAdapter) constructor.newInstance(tts);
                AudioManager.registerTTSAdapter(tts.getName(), adapter);
            } catch (Exception e) {
                logger.error("registerTTS ("+tts.getName()+")error");
            }
        });

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
        VectorStore vectorStore = VectorStoreManager.getVectorStore();
        System.out.println(vectorStore);
    }
}

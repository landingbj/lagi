package ai.servlet;

import ai.annotation.*;
import ai.common.ModelService;
import ai.common.pojo.Backend;
import ai.common.pojo.Driver;
import ai.config.ContextLoader;
import ai.config.GlobalConfigurations;
import ai.dto.ModelInfo;
import ai.dto.ModelPreferenceDto;
import ai.manager.*;
import ai.migrate.dao.UserModelPreferenceDao;
import ai.servlet.annotation.Body;
import ai.servlet.annotation.Get;
import ai.servlet.annotation.Param;
import ai.servlet.annotation.Post;
import cn.hutool.core.annotation.AnnotationUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.google.common.collect.Lists;
import lombok.Builder;
import lombok.Data;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.lang.annotation.Annotation;
import java.util.*;
import java.util.stream.Collectors;

public class PreferenceServlet extends RestfulServlet {

    private UserModelPreferenceDao userModelPreferenceDao = new UserModelPreferenceDao();

    private final Map<String, List<ModelInfo>> modelInfoMap = new HashMap<>();

    private GlobalConfigurations configuration = (GlobalConfigurations) ContextLoader.configuration;

    @Data
    @Builder
    static class ModelDriver  {
        private String model;
        private Class<?> driver;
        private Boolean enable;
        private String description;
        private String company;
    }

    public PreferenceServlet(){
        List<Backend> models = configuration.getModels();
        List<ModelDriver> drivers = models.stream()
                .flatMap(m-> m.getDrivers().stream().map(d->{
                    boolean enable = checkApiKey(m, d);
                    String description = enable ? "" : "缺少必要的apikey ...";
                    Class<?> clazz = null;
                    try {
                        clazz = Class.forName(d.getDriver());
                    } catch (ClassNotFoundException ignored) {
                    }
                    ModelDriver build = ModelDriver.builder().enable(enable).description(description).company(m.getName()).driver(clazz).build();
                    return clazz == null ? null : build;
                }))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        List<ModelInfo> aLLMs = genModelInfo(drivers, LLM.class);
        List<ModelInfo> aTTSs = genModelInfo(drivers, TTS.class);
        List<ModelInfo> aASRs = genModelInfo(drivers, ASR.class);
        List<ModelInfo> aImg2Texts = genModelInfo(drivers, Img2Text.class);
        List<ModelInfo> aImgGens = genModelInfo(drivers, ImgGen.class);
        List<ModelInfo> aImgEnhances = genModelInfo(drivers, ImgEnhance.class);
        List<ModelInfo> aImg2Videos = genModelInfo(drivers,Img2Video.class);
        List<ModelInfo> aText2Videos = genModelInfo(drivers,Text2Video.class);
        List<ModelInfo> aVideoEnhances = genModelInfo(drivers,VideoEnhance.class);
        List<ModelInfo> aVideoTracks = genModelInfo(drivers,VideoTrack.class);
        modelInfoMap.put("llm", aLLMs);
        modelInfoMap.put("tts", aTTSs);
        modelInfoMap.put("asr", aASRs);
        modelInfoMap.put("img2Text", aImg2Texts);
        modelInfoMap.put("imgGen", aImgGens);
        modelInfoMap.put("imgEnhance", aImgEnhances);
        modelInfoMap.put("img2Video", aImg2Videos);
        modelInfoMap.put("text2Video", aText2Videos);
        modelInfoMap.put("videoEnhance", aVideoEnhances);
        modelInfoMap.put("videoTrack", aVideoTracks);

    }

    private <A extends Annotation> List<ModelInfo> genModelInfo(List<ModelDriver> drivers, Class<A> annotationClass) {
        return drivers.stream()
                .filter(md-> md.getDriver().getAnnotation(annotationClass) != null)
                .flatMap(md-> {
                    String [] modelNames = AnnotationUtil.getAnnotationValue(md.driver, annotationClass, "modelNames");
                    return Arrays.stream(modelNames)
                            .map(m->
                                    ModelInfo.builder().model(m).enabled(md.getEnable()).company(md.getCompany()).description(md.getDescription()).build()
                            );
                })
                .collect(Collectors.toList());
    }

    private boolean checkApiKey(Backend model, Driver driver) {
         String appId = model.getAppId() == null ? driver.getAppId() : model.getAppId();
         String apiKey = model.getApiKey() == null ? driver.getApiKey() : model.getApiKey();
         String secretKey = model.getSecretKey() == null ? driver.getSecretKey() : model.getSecretKey();
//         String appKey = model.getAppKey() == null ? driver.getAppKey() : model.getAppKey();
         String accessKeyId = model.getAccessKeyId() == null ? driver.getAccessKeyId() : model.getAccessKeyId();
         String accessKeySecret = model.getAccessKeySecret() == null ? driver.getAccessKeySecret() : model.getAccessKeySecret();
         String securityKey = model.getSecurityKey() == null ? driver.getSecurityKey() : model.getSecurityKey();
         boolean res = false;
         if(apiKey != null) {
             if(secretKey != null) {
                 res =  (!apiKey.startsWith("you")) && (!secretKey.startsWith("you"));
             } else {
                 res =  !apiKey.startsWith("you");
             }
         }
        if(accessKeyId != null && accessKeySecret != null) {
            res =  (!accessKeyId.startsWith("you")) && !(accessKeySecret.startsWith("you"));
        }
        if(appId != null && securityKey != null) {
            res =  !(securityKey.startsWith("you"));
        }
        return res;
    }

    private void setActivate(List<ModelInfo> orDefault, String activeModel) {
        orDefault.stream().filter(o->o.getModel().equals(activeModel)).findAny().ifPresent(o->{
            o.setActivate(true);
        });
    }

    private String getModelName(String userModel, String defaultModel) {
        return userModel == null ? defaultModel : userModel;
    }

    private <T> String getModelService(AIManager<T> aiManager) {
        if(!aiManager.getAdapters().isEmpty()) {
            ModelService modelService = (ModelService) aiManager.getAdapters().get(0);
            return modelService.getModel();
        }
        return null;
    }

    @Get("getModels")
    public List<ModelInfo> getModels(@Param("type") String type, @Param("userId") String userId, HttpServletRequest request) {
        ModelPreferenceDto preferenceRequest = loadUserPreference(userId, request);
        List<ModelInfo> orDefault = modelInfoMap.getOrDefault(type, Collections.emptyList()).stream()
                .map(ObjectUtil::cloneByStream)
                .collect(Collectors.toList());

        if("llm".equals(type)) {
            setActivate(orDefault, getModelName(preferenceRequest.getLlm(), getModelService(LlmManager.getInstance())));
        }
        if("asr".equals(type)) {
            setActivate(orDefault, getModelName(preferenceRequest.getAsr(), getModelService(ASRManager.getInstance())));
        }
        if("tts".equals(type)) {
            setActivate(orDefault, getModelName(preferenceRequest.getTts(), getModelService(TTSManager.getInstance())));
        }
        if("img2Text".equals(type)) {
            setActivate(orDefault, getModelName(preferenceRequest.getImg2Text(), getModelService(Image2TextManger.getInstance())));
        }
        if("imgGen".equals(type)) {
            setActivate(orDefault, getModelName(preferenceRequest.getImgGen(), getModelService(ImageGenerationManager.getInstance())));
        }
        if("imgEnhance".equals(type)) {
            setActivate(orDefault, getModelName(preferenceRequest.getImgEnhance(), getModelService(ImageEnhanceManager.getInstance())));
        }
        if("img2Video".equals(type)) {
            setActivate(orDefault, getModelName(preferenceRequest.getImg2Video(), getModelService(Image2VideoManager.getInstance())));
        }
        if("text2Video".equals(type)) {
            setActivate(orDefault, getModelName(preferenceRequest.getText2Video(), getModelService(Text2VideoManager.getInstance())));
        }
        if("videoEnhance".equals(type)) {
            setActivate(orDefault, getModelName(preferenceRequest.getVideoEnhance(), getModelService(Video2EnhanceManger.getInstance())));
        }
        if("videoTrack".equals(type)) {
            setActivate(orDefault, getModelName(preferenceRequest.getVideoTrack(), getModelService(Video2TrackManager.getInstance())));
        }
        return orDefault;
    }

    private ModelPreferenceDto loadUserPreference(String fingerId, HttpServletRequest request) {
        HttpSession session = request.getSession();
        Object preference = session.getAttribute("preference");
        ModelPreferenceDto userModelPreference = null;
        if(preference == null) {
            userModelPreference = userModelPreferenceDao.getUserModelPreference(fingerId);
            session.setAttribute("preference", JSONUtil.toJsonStr(userModelPreference));
        } else {
            userModelPreference = JSONUtil.toBean((String) preference, ModelPreferenceDto.class);
        }
        return userModelPreference;
    }


    @Post("savePreference")
    public Integer savePreference(@Body ModelPreferenceDto preferenceRequest, HttpServletRequest request) {
        if(preferenceRequest.getLlm() != null) {
            registerModel("llm", preferenceRequest.getLlm(), LlmManager.getInstance());
        }
        if(preferenceRequest.getAsr() != null){
            registerModel("asr", preferenceRequest.getAsr(), ASRManager.getInstance());
        }
        if(preferenceRequest.getTts() != null){
            registerModel("tts", preferenceRequest.getTts(), TTSManager.getInstance());
        }
        if(preferenceRequest.getImg2Text() != null){
            registerModel("img2Text", preferenceRequest.getImg2Text(), Image2TextManger.getInstance());
        }
        if(preferenceRequest.getImgGen() != null) {
            registerModel("imgGen", preferenceRequest.getImgGen(), ImageGenerationManager.getInstance());
        }
        if(preferenceRequest.getImgEnhance() != null) {
            registerModel("imgEnhance", preferenceRequest.getImgEnhance(), ImageEnhanceManager.getInstance());
        }
        if(preferenceRequest.getImg2Video() != null) {
            registerModel("img2Video", preferenceRequest.getImg2Video(), Image2VideoManager.getInstance());
        }
        if(preferenceRequest.getText2Video() != null) {
            registerModel("text2Video", preferenceRequest.getText2Video(), Text2VideoManager.getInstance());
        }
        if(preferenceRequest.getVideoEnhance() != null) {
            registerModel("videoEnhance", preferenceRequest.getVideoEnhance(), Video2EnhanceManger.getInstance());
        }
        if(preferenceRequest.getVideoTrack() != null) {
            registerModel("videoTrack", preferenceRequest.getVideoTrack(), Video2TrackManager.getInstance());
        }
        Integer res = userModelPreferenceDao.saveOrUpdate(preferenceRequest);
        request.getSession().removeAttribute("preference");
        loadUserPreference(preferenceRequest.getFinger(), request);
        return res;
    }

    private <T> void registerModel(String type,  String model, AIManager<T> aiManager) {
        modelInfoMap.get(type).stream().filter(mi->mi.getModel().equals(model) && mi.getEnabled()).findAny().ifPresent(mi->{
            aiManager.register(configuration.getModels(),Lists.newArrayList(Backend.builder()
                    .backend(mi.getCompany())
                    .model(mi.getModel())
                    .enable(true)
                    .priority(1)
                    .build()));
        });
    }

    @Get("getPreference")
    public ModelPreferenceDto getPreference(@Param("userId") String userId) {
        return userModelPreferenceDao.getUserModelPreference(userId);
    }


    @Get("clearPreference")
    public Integer clearPreference(@Param("userId") String userId, HttpServletRequest request) {
        Integer res = userModelPreferenceDao.remove(userId);
        if(res > 0) {
            HttpSession session = request.getSession();
            session.removeAttribute("preference");
        }
        return res;
    }

}

package ai.servlet;

import ai.annotation.*;
import ai.common.pojo.Backend;
import ai.config.ContextLoader;
import ai.config.GlobalConfigurations;
import ai.dto.ModelInfo;
import ai.dto.ModelPreferenceDto;
import ai.migrate.dao.UserModelPreferenceDao;
import ai.servlet.annotation.Body;
import ai.servlet.annotation.Get;
import ai.servlet.annotation.Param;
import ai.servlet.annotation.Post;
import cn.hutool.json.JSONUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.*;
import java.util.stream.Collectors;

public class PreferenceServlet extends RestfulServlet {

    UserModelPreferenceDao userModelPreferenceDao = new UserModelPreferenceDao();

    private final Map<String, List<ModelInfo>> modelInfoMap = new HashMap<>();

    public PreferenceServlet(){
        GlobalConfigurations configuration = (GlobalConfigurations) ContextLoader.configuration;
        List<Backend> models = configuration.getModels();
        List<Class<?>> drivers = models.stream()
                .flatMap(m-> m.getDrivers().stream())
                .map(d->{
                    try {
                        return Class.forName(d.getDriver());
                    } catch (ClassNotFoundException e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull).collect(Collectors.toList());

        List<ModelInfo> aLLMs = drivers.stream()
                .filter(cls-> cls.getAnnotation(LLM.class) != null)
                .flatMap(cls-> Arrays.stream(cls.getAnnotation(LLM.class).modelName()))
                .map(m-> ModelInfo.builder().model(m).build())
                .collect(Collectors.toList());
        List<ModelInfo> aTTSs = drivers.stream()
                .filter(cls-> cls.getAnnotation(TTS.class) != null)
                .flatMap(cls-> Arrays.stream(cls.getAnnotation(TTS.class).modelNames()))
                .map(m-> ModelInfo.builder().model(m).build())
                .collect(Collectors.toList());
        List<ModelInfo> aASRs = drivers.stream()
                .filter(cls-> cls.getAnnotation(ASR.class) != null)
                .flatMap(cls-> Arrays.stream(cls.getAnnotation(ASR.class).modelNames()))
                .map(m-> ModelInfo.builder().model(m).build())
                .collect(Collectors.toList());
        List<ModelInfo> aImg2Texts = drivers.stream()
                .filter(cls-> cls.getAnnotation(Img2Text.class) != null)
                .flatMap(cls-> Arrays.stream(cls.getAnnotation(Img2Text.class).modelNames()))
                .map(m-> ModelInfo.builder().model(m).build())
                .collect(Collectors.toList());
        List<ModelInfo> aImgGens = drivers.stream()
                .filter(cls-> cls.getAnnotation(ImgGen.class) != null)
                .flatMap(cls-> Arrays.stream(cls.getAnnotation(ImgGen.class).modelNames()))
                .map(m-> ModelInfo.builder().model(m).build())
                .collect(Collectors.toList());
        List<ModelInfo> aImgEnhances = drivers.stream()
                .filter(cls-> cls.getAnnotation(ImgEnhance.class) != null)
                .flatMap(cls-> Arrays.stream(cls.getAnnotation(ImgEnhance.class).modelNames()))
                .map(m-> ModelInfo.builder().model(m).build())
                .collect(Collectors.toList());
        List<ModelInfo> aImg2Videos = drivers.stream()
                .filter(cls-> cls.getAnnotation(Img2Video.class) != null)
                .flatMap(cls-> Arrays.stream(cls.getAnnotation(Img2Video.class).modelNames()))
                .map(m-> ModelInfo.builder().model(m).build())
                .collect(Collectors.toList());
        List<ModelInfo> aText2Videos = drivers.stream()
                .filter(cls-> cls.getAnnotation(Text2Video.class) != null)
                .flatMap(cls-> Arrays.stream(cls.getAnnotation(Text2Video.class).modelNames()))
                .map(m-> ModelInfo.builder().model(m).build())
                .collect(Collectors.toList());
        List<ModelInfo> aVideoEnhances = drivers.stream()
                .filter(cls-> cls.getAnnotation(VideoEnhance.class) != null)
                .flatMap(cls-> Arrays.stream(cls.getAnnotation(VideoEnhance.class).modelNames()))
                .map(m-> ModelInfo.builder().model(m).build())
                .collect(Collectors.toList());
        List<ModelInfo> aVideoTracks = drivers.stream()
                .filter(cls-> cls.getAnnotation(VideoTrack.class) != null)
                .flatMap(cls-> Arrays.stream(cls.getAnnotation(VideoTrack.class).modelNames()))
                .map(m-> ModelInfo.builder().model(m).build())
                .collect(Collectors.toList());

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


    @Get("getModels")
    public List<ModelInfo> getModels(@Param("type") String type) {
        return modelInfoMap.getOrDefault(type, Collections.emptyList());
    }



    @Post("savePreference")
    public Integer savePreference(@Body ModelPreferenceDto preferenceRequest) {
        return userModelPreferenceDao.saveOrUpdate(preferenceRequest);
    }

    @Get("getPreference")
    public ModelPreferenceDto getPreference(@Param("userId") String userId) {
        return userModelPreferenceDao.getUserModelPreference(userId);
    }

    @Get("enablePreference")
    public Integer enablePreference(@Param("userId") String userId, HttpServletRequest request) {
        HttpSession session = request.getSession();
        session.setAttribute("preference", JSONUtil.toJsonStr(userModelPreferenceDao.getUserModelPreference(userId)));
        return 1;
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

    public static void main(String[] args) {
        System.out.println(1);
        ContextLoader.loadContext();
        PreferenceServlet preferenceServlet = new PreferenceServlet();
        List<ModelInfo> llm = preferenceServlet.getModels("asr");
        System.out.println(llm);


        ModelPreferenceDto my = ModelPreferenceDto.builder()
                .finger("ba6f6387f85a1d9b5f2e087ab8b01a2a")
                .asr("alibaba-asr")
                .tts("alibaba-tts")
                .build();
        preferenceServlet.savePreference(my);

        ModelPreferenceDto test = preferenceServlet.getPreference("ba6f6387f85a1d9b5f2e087ab8b01a2a");
        System.out.println(test);
    }
}

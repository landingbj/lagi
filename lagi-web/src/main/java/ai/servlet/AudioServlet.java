package ai.servlet;

import ai.lagi.service.AudioService;
import ai.migrate.pojo.Configuration;
import ai.migrate.pojo.TTSRequestParam;
import ai.migrate.pojo.TTSResult;
import ai.migrate.pojo.Text2VoiceEntity;
import ai.utils.DownloadUtils;
import ai.utils.LagiGlobal;
import ai.utils.WhisperResponse;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class AudioServlet extends BaseServlet {
    protected Gson gson = new Gson();
    private static final long serialVersionUID = 1L;
    
    private static LoadingCache<Text2VoiceEntity, String> cache;
    private final static int CACHE_SIZE = 100;
    private final static long EXPIRE_SECONDS = 60 * 60 * 24 * 7;
    
    static {
        cache = initCache(CACHE_SIZE, EXPIRE_SECONDS);
    }
    
    private static Configuration config = LagiGlobal.config;
    private AudioService audioService = new AudioService(config);

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setHeader("Content-Type", "application/json;charset=utf-8");
        String url = req.getRequestURI();
        String method = url.substring(url.lastIndexOf("/") + 1);

        if (method.equals("text2Voice")) {
            this.text2Voice(req, resp);
        } else if (method.equals("audioTrain")) {
            this.audioTrain(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doGet(req, resp);
    }

    private void text2Voice(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Text2VoiceEntity text2VoiceEntity = gson.fromJson(requestToJson(req), Text2VoiceEntity.class);
        
        String json = cache.getIfPresent(text2VoiceEntity);
        if (json != null) {
            responsePrint(resp, json);
        } else {
            TTSRequestParam ttsRequestParam = new TTSRequestParam();
            ttsRequestParam.setText(text2VoiceEntity.getText());
            ttsRequestParam.setEmotion(text2VoiceEntity.getEmotion());
            TTSResult result = audioService.tts(ttsRequestParam);
            if (result == null || result.getStatus() == LagiGlobal.TTS_STATUS_FAILURE) {
                resp.sendError(500);
                return;
            }
            String voiceUrl = result.getResult();
            ServletContext context = req.getServletContext();
            String rootPath = context.getRealPath("");
            String filePath = rootPath + "static/voice/";
            File tempDir = new File(filePath);
            if (!tempDir.exists()) {
                tempDir.mkdirs();
            }
            WhisperResponse whisperResponse = null;
            String filename = UUID.randomUUID() + "." + "wav";
            whisperResponse = DownloadUtils.downloadVoiceFile(voiceUrl, "wav", filePath, filename);
            if (whisperResponse.getCode() == 0) {
                resp.sendError(500);
            }
            Map<String, String> map = new HashMap<>();
            map.put("status", "success");
            map.put("data", "static/voice/" + whisperResponse.getMsg());
            json = gson.toJson(map);
            cache.put(text2VoiceEntity, json);
        }
        responsePrint(resp, json);
    }

    private void audioTrain(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        JsonObject jsonResult = new JsonObject();
        jsonResult.addProperty("status", "success");
        PrintWriter out = resp.getWriter();
        out.write(gson.toJson(jsonResult));
        out.flush();
        out.close();
    }

    private static LoadingCache<Text2VoiceEntity, String> initCache(int maximumSize, long expireSeconds) {
        CacheLoader<Text2VoiceEntity, String> loader;
        loader = new CacheLoader<Text2VoiceEntity, String>() {
            @Override
            public String load(Text2VoiceEntity key) {
                return key.toString();
            }
        };
        LoadingCache<Text2VoiceEntity, String> cache = CacheBuilder.newBuilder().maximumSize(maximumSize)
                .expireAfterWrite(expireSeconds, TimeUnit.SECONDS).build(loader);
        return cache;
    }
}

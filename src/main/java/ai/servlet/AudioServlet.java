package ai.servlet;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import ai.lagi.service.AudioService;
import ai.learning.pojo.Response;
import ai.migrate.pojo.Configuration;
import ai.migrate.pojo.TTSRequestParam;
import ai.migrate.pojo.TTSResult;
import ai.migrate.pojo.Text2VoiceEntity;
import ai.utils.DownloadUtils;
import ai.utils.LagiGlobal;
import ai.utils.MigrateGlobal;
import ai.utils.WhisperResponse;

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

//    private AudioService audioService = new AudioService();

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
        } else if (method.equals("getProgressInfo")) {
//            this.getProgressInfo(req, resp);
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
            TTSResult result = audioService.tts(ttsRequestParam);
//            String result = audioService.text2Voice(text2VoiceEntity);
            if (result == null || result.getStatus() == LagiGlobal.TTS_STATUS_FAILURE) {
                resp.sendError(500);
            }
            String voiceUrl = result.getResult();
            ServletContext context = req.getServletContext();
            String rootPath = context.getRealPath("");
            String filePath = rootPath + "static/voice/";
            File tempDir = new File(filePath);
            if (!tempDir.exists()) {
                tempDir.mkdirs(); // 创建临时文件夹及其父文件夹（如果不存在）
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
//        DiskFileItemFactory factory = new DiskFileItemFactory();
//        ServletFileUpload upload = new ServletFileUpload(factory);
//        upload.setFileSizeMax(MigrateGlobal.AUDIO_FILE_SIZE_LIMIT);
//        upload.setSizeMax(MigrateGlobal.AUDIO_FILE_SIZE_LIMIT);
//        String filePath = getServletContext().getRealPath("/audioUpload");
//        if (!new File(filePath).isDirectory()) {
//            new File(filePath).mkdirs();
//        }
//        List<File> files = new ArrayList<>();
//        List<?> fileItems;
//        JsonObject trainRequest = new JsonObject();
//        try {
//            fileItems = upload.parseRequest(req);
//            Iterator<?> it = fileItems.iterator();
//            while (it.hasNext()) {
//                FileItem fi = (FileItem) it.next();
//                if (!fi.isFormField()) {
//                    String fileName = fi.getName();
//                    File file = null;
//                    String newName = null;
//                    do {
//                        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
//                        newName = sdf.format(new Date()) + ("" + Math.random()).substring(2, 6);
//                        newName = newName + fileName.substring(fileName.lastIndexOf("."));
//                        file = new File(filePath + File.separator + newName);
//                    } while (file.exists());
//                    fi.write(file);
//                    files.add(file);
//                } else {
//                    trainRequest.addProperty(fi.getFieldName(), fi.getString());
//                }
//            }
//        } catch (FileUploadException e1) {
//            e1.printStackTrace();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

//        String uploadDir = audioService.uploadTrainFiles(files, "wonder");
//        if (uploadDir != null) {
//            trainRequest.addProperty("inp_root", uploadDir);
//            String result = audioService.train(trainRequest);
//            if (result != null) {
//                jsonResult.addProperty("status", "success");
//            }
//        }

        PrintWriter out = resp.getWriter();
        out.write(gson.toJson(jsonResult));
        out.flush();
        out.close();
    }

//    private void getProgressInfo(HttpServletRequest req, HttpServletResponse resp) throws IOException {
//        req.setCharacterEncoding("utf-8");
//        resp.setContentType("application/json;charset=utf-8");
//        String category = req.getParameter("category");
//        String result = audioService.getProgressInfo(category);
//        PrintWriter out = resp.getWriter();
//        if (result == null) {
//            resp.sendError(500);
//        } else {
//            out.print(result);
//            out.flush();
//            out.close();
//        }
//    }
    
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

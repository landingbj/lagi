package ai.servlet.api;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ai.audio.service.AudioService;
import ai.common.pojo.AsrResult;
import ai.common.pojo.AudioRequestParam;
import ai.common.pojo.Configuration;
import ai.common.pojo.TTSRequestParam;
import ai.common.pojo.TTSResult;
import ai.servlet.BaseServlet;
import ai.utils.FileUploadUtil;
import ai.utils.LagiGlobal;
import ai.utils.MigrateGlobal;

public class AudioApiServlet extends BaseServlet {
    private static final long serialVersionUID = 1L;

    private static Configuration config = MigrateGlobal.config;

    private AudioService audioService = new AudioService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setHeader("Content-Type", "application/json;charset=utf-8");
        String url = req.getRequestURI();
        String method = url.substring(url.lastIndexOf("/") + 1);
        if (method.equals("asr") || method.equals("speech2text")) {
            this.asr(req, resp);
        } else if (method.equals("tts") || method.equals("text2speech")) {
            this.tts4Post(req, resp);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setHeader("Content-Type", "application/json;charset=utf-8");
        String url = req.getRequestURI();
        String method = url.substring(url.lastIndexOf("/") + 1);
        if (method.equals("tts") || method.equals("text2speech")) {
            this.tts(req, resp);
        }
    }

    private void asr(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=utf-8");
        AudioRequestParam audioRequestParam = postQueryToObj(req, AudioRequestParam.class);
        String uploadDir = getServletContext().getRealPath("/upload");
        if (!new File(uploadDir).isDirectory()) {
            new File(uploadDir).mkdirs();
        }
        String extension = "wav";
        if (audioRequestParam.getFormat() != null) {
            extension = audioRequestParam.getFormat();
        }
        String audioFilePath = uploadDir + "/" + FileUploadUtil.generateRandomFileName(extension);
        try (InputStream inputStream = req.getInputStream();
                OutputStream outputStream = new FileOutputStream(audioFilePath)) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        AsrResult result = audioService.asr(audioFilePath, audioRequestParam);
        responsePrint(resp, toJson(result));
    }

    private void tts(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=utf-8");
        TTSRequestParam ttsRequestParam = queryToObj(req, TTSRequestParam.class);
        TTSResult result = audioService.tts(ttsRequestParam);
        if (result.getStatus() == LagiGlobal.TTS_STATUS_FAILURE) {
            responsePrint(resp, toJson(result));
        } else {
            resp.setContentType("audio/mpeg");
            URL url = new URL(result.getResult());
            try (BufferedInputStream in = new BufferedInputStream(url.openStream());
                    OutputStream out = resp.getOutputStream()) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
            } catch (IOException e) {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        }
    }

    private void tts4Post(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=utf-8");
        TTSRequestParam ttsRequestParam = reqBodyToObj(req, TTSRequestParam.class);
        TTSResult result = audioService.tts(ttsRequestParam);
        responsePrint(resp, toJson(result));
    }

}

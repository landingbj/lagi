package ai.servlet.api;

import ai.llm.service.CompletionsService;
import ai.openai.pojo.ChatCompletionChoice;
import ai.openai.pojo.ChatCompletionRequest;
import ai.openai.pojo.ChatCompletionResult;
import ai.servlet.BaseServlet;
import ai.utils.LRUCache;
import ai.utils.SensitiveWordUtil;
import ai.worker.pojo.GenerateEssayRequest;
import ai.worker.pojo.UploadFile;
import ai.worker.pojo.VideoSummaryRequest;
import ai.worker.zhipu.MedicineWorker;
import com.google.gson.reflect.TypeToken;
import io.reactivex.Observable;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.util.*;

public class MedicineApiServlet extends BaseServlet {
    private static final long serialVersionUID = 1L;
    private static final String UPLOAD_DIR = MedicineWorker.getUploadDir();
    private static final Logger logger = LoggerFactory.getLogger(MedicineApiServlet.class);
    private final CompletionsService completionsService = new CompletionsService();
    private final MedicineWorker medicineWorker = new MedicineWorker();
    private static final LRUCache<String, String> fileCache = new LRUCache<>(1000);

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        String url = req.getRequestURI();
        String method = url.substring(url.lastIndexOf("/") + 1);

        switch (method) {
            case "uploadVideoFiles":
                this.uploadVideoFiles(req, resp);
                break;
            case "uploadTextFiles":
                this.uploadTextFiles(req, resp);
                break;
            case "videoToText":
                this.videoToText(req, resp);
                break;
            case "generateVideoSummary":
                this.generateVideoSummary(req, resp);
                break;
            case "generateEssay":
                this.generateEssay(req, resp);
                break;
            case "prepareForGenerateEssay":
                this.prepareForGenerateEssay(req, resp);
                break;
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        String url = req.getRequestURI();
        String method = url.substring(url.lastIndexOf("/") + 1);

        if (method.equals("getGenerateEssayProgress")) {
            this.getGenerateEssayProgress(req, resp);
        }
    }

    private void uploadVideoFiles(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=utf-8");
        long fileSizeMax = 2L * 1024 * 1024 * 1024;
        long sizeMax = 2L * 1024 * 1024 * 1024;
        List<UploadFile> fileList = uploadFiles(req, fileSizeMax, sizeMax);
        Map<String, Object> map = new HashMap<>();
        if (!fileList.isEmpty()) {
            map.put("status", "success");
            map.put("data", fileList);
        } else {
            map.put("status", "failed");
        }
        responsePrint(resp, gson.toJson(map));
    }

    private void getGenerateEssayProgress(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=utf-8");
        String taskId = req.getParameter("taskId");
        Map<String, Object> map = new HashMap<>();
        map.put("status", "success");
        map.put("data", medicineWorker.getGenerateEssayProgress(taskId));
        responsePrint(resp, gson.toJson(map));
    }

    private void prepareForGenerateEssay(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=utf-8");
        String json = requestToJson(req);
        GenerateEssayRequest request = gson.fromJson(json, GenerateEssayRequest.class);
        List<UploadFile> uploadFileList = request.getUploadFileList();
        String emphasis = request.getEmphasis();
        String taskId = UUID.randomUUID().toString();
        new Thread(() -> medicineWorker.prepareForGenerateEssay(taskId, emphasis, uploadFileList)).start();
        Map<String, Object> map = new HashMap<>();
        map.put("status", "success");
        map.put("data", uploadFileList);
        map.put("taskId", taskId);
        responsePrint(resp, gson.toJson(map));
    }

    private void generateVideoSummary(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setHeader("Content-Type", "text/event-stream;charset=utf-8");
        PrintWriter out = resp.getWriter();

        String json = requestToJson(req);
        VideoSummaryRequest videoSummaryRequest = gson.fromJson(json, VideoSummaryRequest.class);
        List<UploadFile> uploadFileList = videoSummaryRequest.getUploadFileList();
        String emphasis = videoSummaryRequest.getEmphasis();

        for (UploadFile uploadFile : uploadFileList) {
            String text = fileCache.get(uploadFile.getFileName());
            if (text != null) {
                uploadFile.setText(text);
            }
        }
        ChatCompletionRequest chatCompletionRequest = medicineWorker.getVideoSubtitlePrompt(videoSummaryRequest);
        streamOutPrint(chatCompletionRequest, out);
        out.print("data: " + "[DONE]" + "\n\n");
        out.flush();
        out.close();
    }

    private void uploadTextFiles(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=utf-8");
        long fileSizeMax = 30 * 1024 * 1024;
        long sizeMax = 150 * 1024 * 1024;
        List<UploadFile> fileList = uploadFiles(req, fileSizeMax, sizeMax);

        Map<String, Object> map = new HashMap<>();
        if (!fileList.isEmpty()) {
            map.put("status", "success");
            map.put("data", fileList);
        } else {
            map.put("status", "failed");
        }
        responsePrint(resp, gson.toJson(map));
    }

    private List<UploadFile> uploadFiles(HttpServletRequest req, long fileSizeMax, long sizeMax) {
        DiskFileItemFactory factory = new DiskFileItemFactory();
        ServletFileUpload upload = new ServletFileUpload(factory);
        upload.setFileSizeMax(fileSizeMax);
        upload.setSizeMax(sizeMax);
        String uploadDir = getServletContext().getRealPath(UPLOAD_DIR);
        if (!new File(uploadDir).isDirectory()) {
            new File(uploadDir).mkdirs();
        }

        List<UploadFile> fileList = new ArrayList<>();
        try {
            List<?> fileItems = upload.parseRequest(req);
            for (Object fileItem : fileItems) {
                FileItem fi = (FileItem) fileItem;
                if (!fi.isFormField()) {
                    String ext = fi.getName().substring(fi.getName().lastIndexOf("."));
                    String newName = UUID.randomUUID().toString().replace("-", "") + ext;
                    String filePath = uploadDir + File.separator + newName;
                    File file = new File(filePath);
                    fi.write(file);
                    UploadFile uploadFile = UploadFile.builder().fileName(newName).realName(fi.getName()).filePath(filePath).build();
                    fileList.add(uploadFile);
                }
            }
        } catch (Exception e) {
            logger.error("", e);
        }
        return fileList;
    }


    private void videoToText(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=utf-8");
        String json = requestToJson(req);
        Type listType = new TypeToken<ArrayList<UploadFile>>() {
        }.getType();
        List<UploadFile> uploadFileList = gson.fromJson(json, listType);
        List<String> textList = new ArrayList<>();
        for (UploadFile uploadFile : uploadFileList) {
            String videoPath = uploadFile.getFilePath();
            String text = "medicineWorker.getVideoSubtitle(videoPath);";
            uploadFile.setText(text);
            textList.add(text);
            fileCache.put(uploadFile.getFileName(), text);
        }

        Map<String, Object> map = new HashMap<>();
        if (!textList.isEmpty()) {
            map.put("status", "success");
            map.put("data", uploadFileList);
        } else {
            map.put("status", "failed");
        }
        responsePrint(resp, gson.toJson(map));
    }

    private void generateEssay(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setHeader("Content-Type", "text/event-stream;charset=utf-8");
        PrintWriter out = resp.getWriter();
        resp.setContentType("application/json;charset=utf-8");
        String json = requestToJson(req);
        GenerateEssayRequest request = gson.fromJson(json, GenerateEssayRequest.class);

        ChatCompletionRequest chatCompletionRequest = medicineWorker.getPolishCompletionRequest(request.getTaskId());
        String result = streamOutPrint(chatCompletionRequest, out);
        medicineWorker.saveText(request.getTaskId(), "essay.md", result);

        out.print("data: " + "[DONE]" + "\n\n");
        out.flush();
        out.close();
    }

    private String streamOutPrint(ChatCompletionRequest chatCompletionRequest, PrintWriter out) {
        Observable<ChatCompletionResult> observable = completionsService.streamCompletions(chatCompletionRequest);
        final ChatCompletionResult[] lastResult = {null, null};

        StringBuilder sb = new StringBuilder();
        observable.subscribe(
                data -> {
                    lastResult[0] = data;
                    ChatCompletionResult filter = SensitiveWordUtil.filter(data);

                    sb.append(filter.getChoices().get(0).getMessage().getContent());

                    String msg = gson.toJson(filter);
                    out.print("data: " + msg + "\n\n");
                    out.flush();
                    if (lastResult[1] == null) {
                        lastResult[1] = data;
                    } else {
                        for (int i = 0; i < lastResult[1].getChoices().size(); i++) {
                            ChatCompletionChoice choice = lastResult[1].getChoices().get(i);
                            ChatCompletionChoice chunkChoice = data.getChoices().get(i);
                            String chunkContent = chunkChoice.getMessage().getContent();
                            String content = choice.getMessage().getContent();
                            choice.getMessage().setContent(content + chunkContent);
                        }
                    }
                },
                e -> {
                    logger.error("", e);
                    out.print("data: " + "[DONE]" + "\n\n");
                },
                () -> {
                    if (lastResult[0] == null) {
                        out.print("data: " + "[DONE]" + "\n\n");
                        return;
                    }
                    lastResult[0].getChoices().get(0).getMessage().setContent("\n\n");
                    String msg = gson.toJson(lastResult[0]);
                    out.print("data: " + msg + "\n\n");
                }
        );
        return sb.toString();
    }
}

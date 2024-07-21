package ai.servlet.api;

import ai.common.utils.FileUtils;
import ai.llm.service.CompletionsService;
import ai.ocr.OcrService;
import ai.ocr.pojo.OcrProgress;
import ai.openai.pojo.ChatCompletionChoice;
import ai.openai.pojo.ChatCompletionRequest;
import ai.openai.pojo.ChatCompletionResult;
import ai.servlet.BaseServlet;
import ai.servlet.dto.AddIndexProgress;
import ai.utils.qa.ChatCompletionUtil;
import ai.worker.pojo.AuditFile;
import ai.servlet.dto.AuditTask;
import ai.utils.LRUCache;
import ai.utils.LagiGlobal;
import ai.utils.SensitiveWordUtil;
import ai.vector.VectorStoreService;
import ai.vector.pojo.IndexRecord;
import ai.worker.llm.AuditFileWorker;
import ai.worker.pojo.AuditPrompt;
import com.google.gson.Gson;
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
import java.util.*;
import java.util.concurrent.TimeUnit;

public class AuditApiServlet extends BaseServlet {
    private static final long serialVersionUID = 1L;
    private static final String UPLOAD_DIR = "/upload";
    private static final LRUCache<String, List<AuditFile>> auditFileCache = new LRUCache<>(1000);
    private static final LRUCache<String, AddIndexProgress> addIndexCache = new LRUCache<>(1000);
    private static final Logger logger = LoggerFactory.getLogger(AuditApiServlet.class);
    private final CompletionsService completionsService = new CompletionsService();
    private final AuditFileWorker auditFileWorker = new AuditFileWorker();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        String url = req.getRequestURI();
        String method = url.substring(url.lastIndexOf("/") + 1);

        if (method.equals("uploadFiles")) {
            this.uploadFiles(req, resp);
        } else if (method.equals("processAuditFile")) {
            this.processAuditFile(req, resp);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        String url = req.getRequestURI();
        String method = url.substring(url.lastIndexOf("/") + 1);

        if (method.equals("getAuditFileStatus")) {
            this.getAuditFileStatus(req, resp);
        } else if (method.equals("getAddIndexProgress")) {
            this.getAddIndexProgress(req, resp);
        }
    }

    private void uploadFiles(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=utf-8");
        String category = req.getParameter("category");

        System.out.println("category: " + category);

        DiskFileItemFactory factory = new DiskFileItemFactory();
        ServletFileUpload upload = new ServletFileUpload(factory);
        upload.setFileSizeMax(50 * 1024 * 1024);
        upload.setSizeMax(50 * 1024 * 1024);
        String uploadDir = getServletContext().getRealPath(UPLOAD_DIR);
        if (!new File(uploadDir).isDirectory()) {
            new File(uploadDir).mkdirs();
        }

        List<File> fileList = new ArrayList<>();
        List<AuditFile> auditFileList = new ArrayList<>();
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
                    fileList.add(file);
                    String md5 = FileUtils.md5sum(file);
                    AuditFile auditFile = AuditFile.builder().md5(md5).filename(fi.getName()).build();
                    auditFileList.add(auditFile);
                }
            }
        } catch (Exception e) {
            logger.error("", e);
        }

        String taskId = UUID.randomUUID().toString().replace("-", "");
        auditFileCache.put(taskId, auditFileList);

        new AuditFileProcess(taskId, fileList, uploadDir).start();

        Map<String, Object> map = new HashMap<>();
        if (category != null) {
            map.put("status", "success");
            map.put("taskId", taskId);
        } else {
            map.put("status", "failed");
        }
        responsePrint(resp, gson.toJson(map));
    }


    private void getAuditFileStatus(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=utf-8");
        String taskId = req.getParameter("taskId");

        List<AuditFile> auditFileList = auditFileCache.get(taskId);
        OcrProgress ocrProgress = ocrService.getOcrProgress(taskId);

        if (ocrProgress != null) {
            for (AuditFile auditFile : auditFileList) {
                String md5 = auditFile.getMd5();
                if (ocrProgress.getMd5().equals(md5)) {
                    ocrProgress.setFilename(auditFile.getFilename());
                    break;
                }
            }
        }

        Map<String, Object> map = new HashMap<>();
        map.put("status", "success");
        map.put("data", ocrProgress);
        responsePrint(resp, gson.toJson(map));
    }

    private void getAddIndexProgress(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=utf-8");
        String taskId = req.getParameter("taskId");
        AddIndexProgress progress = addIndexCache.get(taskId);
        Map<String, Object> map = new HashMap<>();
        map.put("status", "success");
        map.put("data", progress);
        responsePrint(resp, gson.toJson(map));
    }

    private static final OcrService ocrService = new OcrService();

    public static class AuditFileProcess extends Thread {
        private final String taskId;
        private final String uploadDir;
        private final List<File> fileList;

        private final VectorStoreService vectorStoreService = new VectorStoreService();

        public AuditFileProcess(String taskId, List<File> fileList, String uploadDir) {
            this.taskId = taskId;
            this.fileList = fileList;
            this.uploadDir = uploadDir;
        }

        public void run() {
            try {
                List<List<String>> textList = ocrService.recognize(taskId, fileList);
                for (int i = 0; i < fileList.size(); i++) {
                    File file = fileList.get(i);
                    String md5 = FileUtils.md5sum(file);
                    String textFileName = FileUtils.md5sum(file) + ".txt";
                    String filePath = uploadDir + File.separator + textFileName;
                    String text = String.join("", textList.get(i));
                    FileUtils.writeTextToFile(filePath, text);

                    Map<String, String> where = new HashMap<>();
                    where.put("filename", textFileName);
                    List<IndexRecord> recordList = vectorStoreService.fetch(where);

                    if (recordList == null || recordList.isEmpty()) {
                        addDocIndexes(new File(filePath));
                    }

//                    try {
//                        Thread.sleep(2000);
//                    } catch (InterruptedException e) {
//                        throw new RuntimeException(e);
//                    }

                    AddIndexProgress progress = AddIndexProgress.builder()
                            .processedFileSize(i + 1)
                            .totalFileSize(fileList.size())
                            .filename(getRealFilename(md5))
                            .build();
                    addIndexCache.put(taskId, progress);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        private String getRealFilename(String md5) {
            for (AuditFile auditFile : auditFileCache.get(taskId)) {
                if (auditFile.getMd5().equals(md5)) {
                    return auditFile.getFilename();
                }
            }
            return null;
        }

        private void addDocIndexes(File file) {
            Map<String, Object> metadatas = new HashMap<>();
            String fileId = UUID.randomUUID().toString().replace("-", "");
            String category = LagiGlobal.getDefaultCategory();

            metadatas.put("filename", file.getName());
            metadatas.put("category", category);
            metadatas.put("file_id", fileId);
            metadatas.put("level", "user");

            try {
                vectorStoreService.addFileVectors(file, metadatas, category);
            } catch (IOException e) {
                logger.error("", e);
            }
        }
    }


    private void processAuditFile(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=utf-8");
        PrintWriter out = resp.getWriter();
        AuditTask auditTask = reqBodyToObj(req, AuditTask.class);
        resp.setHeader("Content-Type", "text/event-stream;charset=utf-8");
        List<AuditFile> auditFileList = auditFileCache.get(auditTask.getTaskId());
        Date startDate;
        List<AuditPrompt> auditPromptList = auditFileWorker.getAuditPrompts();

        for (int i = 0; i < auditPromptList.size(); i++) {
            System.out.println("start i = " + i);

            ChatCompletionRequest chatCompletionRequest = auditFileWorker.getAuditPrompt(auditFileList, auditPromptList.get(i));
            streamOutPrint(chatCompletionRequest, out);
            startDate = new Date();

            System.out.println("end i = " + i);
            System.out.println();
            System.out.println("end i = " + ChatCompletionUtil.getLastMessage(chatCompletionRequest));
            System.out.println();
            Date now = new Date();
            while (i != auditPromptList.size() - 1 && now.getTime() - startDate.getTime() < 1000 * 61) {
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                now = new Date();
            }

        }
        out.print("data: " + "[DONE]" + "\n\n");
        out.flush();
        out.close();
    }

    private void streamOutPrint(ChatCompletionRequest chatCompletionRequest, PrintWriter out) {
        Observable<ChatCompletionResult> observable = completionsService.streamCompletions(chatCompletionRequest);
        final ChatCompletionResult[] lastResult = {null, null};
        observable.subscribe(
                data -> {
                    lastResult[0] = data;
                    ChatCompletionResult filter = SensitiveWordUtil.filter(data);
                    String msg = gson.toJson(filter);
                    System.out.println("data: " + msg);
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
    }
}

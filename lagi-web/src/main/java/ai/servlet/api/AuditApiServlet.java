package ai.servlet.api;

import ai.common.utils.FileUtils;
import ai.llm.service.CompletionsService;
import ai.ocr.OcrService;
import ai.ocr.PdfPageSizeLimitException;
import ai.ocr.pojo.OcrProgress;
import ai.servlet.BaseServlet;
import ai.servlet.dto.AddIndexProgress;
import ai.worker.pojo.AuditFile;
import ai.utils.LRUCache;
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
import java.util.*;

public class AuditApiServlet extends BaseServlet {
    private static final long serialVersionUID = 1L;
    private static final String UPLOAD_DIR = "/upload";
    private static final LRUCache<String, List<AuditFile>> auditFileCache = new LRUCache<>(1000);
    private static final LRUCache<String, AddIndexProgress> addIndexCache = new LRUCache<>(1000);
    private static final Logger logger = LoggerFactory.getLogger(AuditApiServlet.class);
    private final CompletionsService completionsService = new CompletionsService();
    private static final OcrService ocrService = new OcrService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        String url = req.getRequestURI();
        String method = url.substring(url.lastIndexOf("/") + 1);

        if (method.equals("uploadFiles")) {
            this.uploadFiles(req, resp);
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

        DiskFileItemFactory factory = new DiskFileItemFactory();
        ServletFileUpload upload = new ServletFileUpload(factory);
        upload.setFileSizeMax(50 * 1024 * 1024);
        upload.setSizeMax(50 * 1024 * 1024);
        String uploadDir = getServletContext().getRealPath(UPLOAD_DIR);
        if (!new File(uploadDir).isDirectory()) {
            new File(uploadDir).mkdirs();
        }

        String lang = req.getParameter("lang");

        if (lang == null || lang.isEmpty()) {
            lang = "chn,eng";
        } else if (!lang.equals("eng")) {
            lang = lang + ",eng";
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

        if (fileList.isEmpty()) {
            Map<String, Object> map = new HashMap<>();
            map.put("status", "failed");
            responsePrint(resp, gson.toJson(map));
            return;
        }

        Map<String, Object> map = new HashMap<>();
        List<String> languages = Arrays.asList(lang.split(","));
        List<String> textList = null;
        try {
            textList = ocrService.recognizePdf(fileList.get(0), languages);
        } catch (PdfPageSizeLimitException e) {
            map.put("msg", "PDF文件页数超过限制");
        }
        if (textList != null && !textList.isEmpty()) {
            map.put("status", "success");
            map.put("data", textList);
        } else {
            map.put("status", "failed");
            map.putIfAbsent("msg", "文件解析失败");
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


}

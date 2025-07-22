package ai.servlet;

import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import ai.common.pojo.*;
import ai.dto.ProgressTrackerEntity;
import ai.medusa.MedusaService;
import ai.medusa.pojo.InstructionData;
import ai.medusa.pojo.InstructionPairRequest;
import ai.migrate.service.UploadFileService;
import ai.sevice.KnowledgeBaseService;
import ai.utils.ExcelSqlUtil;
import ai.utils.LRUCacheUtil;
import ai.vector.VectorCacheLoader;
import ai.vector.VectorStoreService;
import ai.vector.pojo.UpsertRecord;
import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonArray;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import ai.vector.FileService;
import ai.vector.VectorDbService;
import ai.utils.MigrateGlobal;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

@Slf4j
public class UploadFileServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final Gson gson = new Gson();
    private final FileService fileService = new FileService();
    private static final Configuration config = MigrateGlobal.config;
    private final VectorDbService vectorDbService = new VectorDbService(config);
    private final UploadFileService uploadFileService = new UploadFileService();
    private final VectorStoreService vectorStoreService = new VectorStoreService();
    private final MedusaService medusaService = new MedusaService();
    private static final String UPLOAD_DIR = "/upload";
    private final KnowledgeBaseService knowledgeBaseService = new KnowledgeBaseService();

    private static final ExecutorService uploadExecutorService = Executors.newFixedThreadPool(5);

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setHeader("Content-Type", "text/html;charset=utf-8");

        String url = req.getRequestURI();
        String method = url.substring(url.lastIndexOf("/") + 1);

        if (method.equals("uploadLearningFile") || method.equals("upload")) {
            this.uploadLearningFile(req, resp);
        } else if (method.equals("downloadFile")) {
            this.downloadFile(req, resp);
        } else if (method.equals("uploadImageFile")) {
            this.uploadImageFile(req, resp);
        } else if (method.equals("uploadVideoFile")) {
            this.uploadVideoFile(req, resp);
        } else if (method.equals("deleteFile")) {
            this.deleteFile(req, resp);
        } else if (method.equals("getUploadFileList")) {
            this.getUploadFileList(req, resp);
        } else if (method.equals("pairing")) {
            this.pairing(req, resp);
        } else if (method.equals("asynchronousUpload")) {
            this.asynchronousUpload(req, resp);
        } else if (method.equals("getProgress")) {
            this.getProgress(req, resp);
        }
    }

    private void uploadLearningFile(HttpServletRequest req, HttpServletResponse resp)  throws ServletException, IOException {
        HttpSession session = req.getSession();
        String category = req.getParameter("category");
        String level = req.getParameter("level");
        String userId = req.getParameter("userId");
        KnowledgeBase knowledgeBase = null;
        String knowledgeBaseId = req.getParameter("knowledgeBaseId");
        if(knowledgeBaseId != null) {
            Long id = Long.valueOf(knowledgeBaseId);
            knowledgeBase = knowledgeBaseService.getById(id);
        }
        JsonObject jsonResult = new JsonObject();
        jsonResult.addProperty("status", "success");
        DiskFileItemFactory factory = new DiskFileItemFactory();
        ServletFileUpload upload = new ServletFileUpload(factory);
        upload.setFileSizeMax(MigrateGlobal.DOC_FILE_SIZE_LIMIT);
        upload.setSizeMax(MigrateGlobal.DOC_FILE_SIZE_LIMIT);
        String uploadDir = getServletContext().getRealPath(UPLOAD_DIR);
        if (!new File(uploadDir).isDirectory()) {
            new File(uploadDir).mkdirs();
        }

        List<File> files = new ArrayList<>();
        Map<String, String> realNameMap = new HashMap<>();

        try {
            List<?> fileItems = upload.parseRequest(req);
            for (Object fileItem : fileItems) {
                FileItem fi = (FileItem) fileItem;
                if (!fi.isFormField()) {
                    String fileName = fi.getName();
                    File file;
                    String newName;
                    do {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
                        newName = sdf.format(new Date()) + ("" + Math.random()).substring(2, 6);
                        newName = newName + fileName.substring(fileName.lastIndexOf("."));
                        String lastFilePath = uploadDir + File.separator + newName;
                        file = new File(lastFilePath);
                        session.setAttribute(newName, file.toString());
                        session.setAttribute("lastFilePath", lastFilePath);
                    } while (file.exists());
                    fi.write(file);
                    files.add(file);
                    realNameMap.put(file.getName(), fileName);
                }
            }
        } catch (Exception ex) {
            jsonResult.addProperty("msg", "解析文件出现错误");
            ex.printStackTrace();
        }
        String taskId = UUID.randomUUID().toString();
        ProgressTrackerEntity tracker = new ProgressTrackerEntity(taskId);
        LRUCacheUtil.put(taskId, tracker);
        List<Future<?>> futures = new ArrayList<>();
        if (!files.isEmpty()) {
            JsonArray fileList = new JsonArray();
            for (File file : files) {
                if (file.exists() && file.isFile()) {
                    String filename = realNameMap.get(file.getName());
                    Future<?> future =uploadExecutorService.submit(new AddDocIndex(file, category, filename, level, userId, taskId, knowledgeBase));
                    futures.add(future);
                    JsonObject jsonObject = new JsonObject();
                    jsonObject.addProperty("filename", filename);
                    jsonObject.addProperty("filepath", file.getName());
                    fileList.add(jsonObject);
                }
            }
            jsonResult.addProperty("data", fileList.toString());
        }
        String status ="success";
        // 等待所有任务完成
        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (InterruptedException e) {
                //任务终断
                status = "failed";
                Thread.currentThread().interrupt();
            } catch (ExecutionException e) {
                //执行中异常
                status = "failed";
                e.printStackTrace();
            }
        }
        if (!jsonResult.has("msg")) {
            jsonResult.addProperty("status", status);
        }
        tracker.setProgress(100);
        LRUCacheUtil.put(taskId, tracker);
        jsonResult.addProperty("task_id", taskId);
        PrintWriter out = resp.getWriter();
        out.write(gson.toJson(jsonResult));
        out.flush();
        out.close();

    }
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doGet(req, resp);
    }

    private void pairing(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        InstructionPairRequest instructionPairRequest = mapper.readValue(requestToJson(req), InstructionPairRequest.class);
        long timestamp = Instant.now().toEpochMilli();
        new Thread(() -> {
            List<InstructionData> instructionDataList = instructionPairRequest.getData();
            String category = instructionPairRequest.getCategory();
            String level = Optional.ofNullable(instructionPairRequest.getLevel()).orElse("user");
            Map<String, String> qaMap = new HashMap<>();
            for (InstructionData data : instructionDataList) {
                for (String instruction : data.getInstruction()) {
                    instruction = instruction.trim();
                    String output = data.getOutput().trim();
                    qaMap.put(instruction, output);
                    Map<String, String> metadata = new HashMap<>();
                    metadata.put("category", category);
                    metadata.put("level", level);
                    metadata.put("filename", "");
                    metadata.put("seq", Long.toString(timestamp));
                    List<UpsertRecord> upsertRecords = new ArrayList<>();
                    upsertRecords.add(UpsertRecord.newBuilder()
                            .withMetadata(metadata)
                            .withDocument(instruction)
                            .build());
                    upsertRecords.add(UpsertRecord.newBuilder()
                            .withMetadata(new HashMap<>(metadata))
                            .withDocument(output)
                            .build());
                    String s = instruction.replaceAll("\n","");
                    VectorCacheLoader.put2L2(s, timestamp, output);
                    vectorStoreService.upsertCustomVectors(upsertRecords, category, true);
                }
            }
            medusaService.load(qaMap, category);
        }).start();

        PrintWriter out = resp.getWriter();
        Map<String, Object> map = new HashMap<>();
        map.put("status", "success");
        out.print(gson.toJson(map));
        out.flush();
        out.close();
    }

    private void deleteFile(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        req.setCharacterEncoding("utf-8");
        resp.setContentType("application/json;charset=utf-8");
        // TODO 2025/7/7 RAG删除:  这里的库为当前文件所在的用户库， 或则不在用户库, 则使用系统库
        String category = req.getParameter("category");
        List<String> idList = gson.fromJson(requestToJson(req), new TypeToken<List<String>>() {
        }.getType());
        if(StrUtil.isBlank(category)) {
            vectorDbService.deleteDoc(idList);
        } else {
            vectorDbService.deleteDoc(idList, category);
        }
        uploadFileService.deleteUploadFile(idList);
        if (ExcelSqlUtil.isConnect()||ExcelSqlUtil.isSqlietConnect()){
            ExcelSqlUtil.deleteListSql(idList);
        }
        Map<String, Object> map = new HashMap<>();
        map.put("status", "success");
        PrintWriter out = resp.getWriter();
        out.print(gson.toJson(map));
        out.flush();
        out.close();
    }

    private void getUploadFileList(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setHeader("Content-Type", "application/json;charset=utf-8");
        int pageSize = Integer.MAX_VALUE;
        int pageNumber = 1;

        String category = req.getParameter("category");
        String userId = req.getParameter("lagiUserId");
        String knowledgeBaseId = req.getParameter("knowledgeBaseId");

        if (req.getParameter("pageNumber") != null) {
            pageSize = Integer.parseInt(req.getParameter("pageSize"));
            pageNumber = Integer.parseInt(req.getParameter("pageNumber"));
        }

        Map<String, Object> map = new HashMap<>();
        List<UploadFile> result = null;
        int totalRow = 0;
        try {
            result = uploadFileService.getUploadFileList(pageNumber, pageSize, category, userId, knowledgeBaseId);
            totalRow = uploadFileService.getTotalRow(category, userId, knowledgeBaseId);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (result != null) {
            map.put("status", "success");
            int totalPage = (int) Math.ceil((double) totalRow / pageSize);
            map.put("totalRow", totalRow);
            map.put("totalPage", totalPage);
            map.put("pageNumber", pageNumber);
            map.put("pageSize", pageSize);
            map.put("data", result);
        } else {
            map.put("status", "failed");
        }
        PrintWriter out = resp.getWriter();
        out.print(gson.toJson(map));
        out.flush();
        out.close();
    }

    // 下载文件
    private void downloadFile(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String filePath = req.getParameter("filePath");
        String fileName = req.getParameter("fileName");
        // 设置响应内容类型
        resp.setContentType("application/pdf");
        String encodedFileName = URLEncoder.encode(fileName, "UTF-8");
        resp.setHeader("Content-Disposition", "attachment; filename=\"" + encodedFileName + "\"");

        String uploadDir = getServletContext().getRealPath(UPLOAD_DIR);
        // 读取文件并写入响应流
        try {
            FileInputStream fileInputStream = new FileInputStream(uploadDir + File.separator + filePath);
            OutputStream outputStream = resp.getOutputStream();
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            fileInputStream.close();
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void uploadVideoFile(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession();

        JsonObject jsonResult = new JsonObject();
        jsonResult.addProperty("status", "failed");
        DiskFileItemFactory factory = new DiskFileItemFactory();
        ServletFileUpload upload = new ServletFileUpload(factory);
        upload.setFileSizeMax(MigrateGlobal.VIDEO_FILE_SIZE_LIMIT);
        upload.setSizeMax(MigrateGlobal.VIDEO_FILE_SIZE_LIMIT);
        String uploadDir = getServletContext().getRealPath(UPLOAD_DIR);
        if (!new File(uploadDir).isDirectory()) {
            new File(uploadDir).mkdirs();
        }

        String lastFilePath = "";

        try {
            // 存储文件
            List<?> fileItems = upload.parseRequest(req);
            Iterator<?> it = fileItems.iterator();

            while (it.hasNext()) {
                FileItem fi = (FileItem) it.next();
                if (!fi.isFormField()) {
                    String fileName = fi.getName();
                    File file;
                    String newName;
                    do {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
                        newName = sdf.format(new Date()) + ("" + Math.random()).substring(2, 6);
                        newName = newName + fileName.substring(fileName.lastIndexOf("."));
                        file = new File(uploadDir + File.separator + newName);
                        lastFilePath = uploadDir + File.separator + newName;
                        session.setAttribute("last_video_file", lastFilePath);
                        jsonResult.addProperty("status", "success");
                    } while (file.exists());
                    fi.write(file);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        PrintWriter out = resp.getWriter();
        out.write(gson.toJson(jsonResult));
        out.flush();
        out.close();
    }

    private void uploadImageFile(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession();

        JsonObject jsonResult = new JsonObject();
        jsonResult.addProperty("status", "failed");
        DiskFileItemFactory factory = new DiskFileItemFactory();
        ServletFileUpload upload = new ServletFileUpload(factory);
        upload.setFileSizeMax(MigrateGlobal.IMAGE_FILE_SIZE_LIMIT);
        upload.setSizeMax(MigrateGlobal.IMAGE_FILE_SIZE_LIMIT);
        String uploadDir = getServletContext().getRealPath(UPLOAD_DIR);
        if (!new File(uploadDir).isDirectory()) {
            new File(uploadDir).mkdirs();
        }

        String lastFilePath = "";

        try {
            // 存储文件
            List<?> fileItems = upload.parseRequest(req);
            Iterator<?> it = fileItems.iterator();

            while (it.hasNext()) {
                FileItem fi = (FileItem) it.next();
                if (!fi.isFormField()) {
                    String fileName = fi.getName();
                    File file = null;
                    String newName = null;
                    do {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
                        newName = sdf.format(new Date()) + ("" + Math.random()).substring(2, 6);
                        newName = newName + fileName.substring(fileName.lastIndexOf("."));
                        file = new File(uploadDir + File.separator + newName);
                        lastFilePath = uploadDir + File.separator + newName;
                        session.setAttribute("last_image_file", lastFilePath);
                        jsonResult.addProperty("status", "success");
                    } while (file.exists());
                    fi.write(file);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        PrintWriter out = resp.getWriter();
        out.write(gson.toJson(jsonResult));
        out.flush();
        out.close();
    }

    private void asynchronousUpload(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession();
        String taskId = UUID.randomUUID().toString();
        ProgressTrackerEntity tracker = new ProgressTrackerEntity(taskId);

        String category = req.getParameter("category");
        String level = req.getParameter("level");
        String userId = req.getParameter("userId");
        KnowledgeBase knowledgeBase = null;
        String knowledgeBaseId = req.getParameter("knowledgeBaseId");
        if(knowledgeBaseId != null) {
            Long id = Long.valueOf(knowledgeBaseId);
            knowledgeBase = knowledgeBaseService.getById(id);
        }

        JsonObject jsonResult = new JsonObject();
        jsonResult.addProperty("status", "success");
        tracker.setProgress(10);
        LRUCacheUtil.put(taskId, tracker);

        jsonResult.addProperty("task_id", taskId);
        PrintWriter out = resp.getWriter();
        out.write(gson.toJson(jsonResult));
        out.flush();
        try {
        DiskFileItemFactory factory = new DiskFileItemFactory();
        ServletFileUpload upload = new ServletFileUpload(factory);
        upload.setFileSizeMax(MigrateGlobal.DOC_FILE_SIZE_LIMIT);
        upload.setSizeMax(MigrateGlobal.DOC_FILE_SIZE_LIMIT);
        String uploadDir = getServletContext().getRealPath(UPLOAD_DIR);
        if (!new File(uploadDir).isDirectory()) {
            new File(uploadDir).mkdirs();
        }

        List<File> files = new ArrayList<>();
        Map<String, String> realNameMap = new HashMap<>();
            List<?> fileItems = upload.parseRequest(req);
            for (Object fileItem : fileItems) {
                FileItem fi = (FileItem) fileItem;
                if (!fi.isFormField()) {
                    String fileName = fi.getName();
                    File file;
                    String newName;
                    do {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
                        newName = sdf.format(new Date()) + ("" + Math.random()).substring(2, 6);
                        newName = newName + fileName.substring(fileName.lastIndexOf("."));
                        String lastFilePath = uploadDir + File.separator + newName;
                        file = new File(lastFilePath);
                        session.setAttribute(newName, file.toString());
                        session.setAttribute("lastFilePath", lastFilePath);
                    } while (file.exists());
                    fi.write(file);
                    files.add(file);
                    realNameMap.put(file.getName(), fileName);
                }
            }

            out.close();
            tracker.setProgress(30);
            LRUCacheUtil.put(taskId, tracker);
            List<Future<?>> futures = new ArrayList<>();
            if (!files.isEmpty()) {
                JsonArray fileList = new JsonArray();
                for (File file : files) {
                    if (file.exists() && file.isFile()) {
                        String filename = realNameMap.get(file.getName());
                        Future<?> future = uploadExecutorService.submit(new AddDocIndex(file, category, filename, level, userId, taskId, knowledgeBase));
                        futures.add(future);
                        JsonObject jsonObject = new JsonObject();
                        jsonObject.addProperty("filename", filename);
                        jsonObject.addProperty("filepath", file.getName());
                        fileList.add(jsonObject);
                    }
                }
                jsonResult.add("data", fileList);
            }
            tracker.setProgress(80);
            LRUCacheUtil.put(taskId, tracker);
        } catch (Exception ex) {
            jsonResult.addProperty("msg", "解析文件出现错误");
            tracker.setProgress(-1);
            LRUCacheUtil.put(taskId, tracker);
            ex.printStackTrace();
        }
    }

    private void getProgress(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setHeader("Content-Type", "application/json;charset=utf-8");
        String taskId = req.getParameter("task_id");
        ProgressTrackerEntity tracker = LRUCacheUtil.get(taskId);
        Map<String, Object> map = new HashMap<>();
        if (tracker != null) {
            map.put("status", "success");
            map.put("progress", tracker.getProgress());
            if (tracker.getProgress() == 100){
                map.put("msg", "上传完毕！");
            }if (tracker.getProgress() >= 70){
                map.put("msg", "模型处理完成，入库中");
            }else if(30 <= tracker.getProgress()) {
                map.put("msg", "正在分析您的文档进行语料注入...");
            }else if(10 <= tracker.getProgress()) {
                map.put("msg", "上传中...");
            }else if(0 > tracker.getProgress()) {
                map.put("status", "failed");
                map.put("msg", "上传失败！");
            }
        } else {
            map.put("status", "failed");
            map.put("msg", "未找到该taskId记录！！！");
        }
        PrintWriter out = resp.getWriter();
        out.print(gson.toJson(map));
        out.flush();
        out.close();
    }

    // TODO 2025/7/7 RAG上传: 改造, 添加 userSelectedParams 属性
    public class AddDocIndex extends Thread {
        private final VectorDbService vectorDbService = new VectorDbService(config);
        private final File file;
        private final String category;
        private final String filename;
        private final String level;
        private final String userId;
        private final String taskId;
        private final KnowledgeBase knowledgeBase;

        public AddDocIndex(File file, String category, String filename, String level, String userId, String taskId, KnowledgeBase knowledgeBase) {
            this.file = file;
            this.category = category;
            this.filename = filename;
            this.level = level;
            this.userId = userId;
            this.taskId = taskId;
            this.knowledgeBase = knowledgeBase;
        }

        public void run() {
            if (vectorDbService.vectorStoreEnabled()) {
                addDocIndexes();
            }
        }

        private void addDocIndexes() {
            Map<String, Object> metadatas = new HashMap<>();
            String fileId = UUID.randomUUID().toString().replace("-", "");
            String filepath = file.getName();

            metadatas.put("filename", filename);
            metadatas.put("category", category);
            metadatas.put("filepath", filepath);
            metadatas.put("file_id", fileId);
            metadatas.put("userId", userId);
            List<UserRagSetting> settingList = null;
            metadatas.put("knowledgeBase", knowledgeBase);
            // TODO 弃用
            try {
                settingList = uploadFileService.getTextBlockSize(category, userId);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            metadatas.put("settingList", settingList);
            if (level == null) {
                metadatas.put("level", "user");
            } else {
                metadatas.put("level", level);
            }
            // 模拟进度更新
            ProgressTrackerEntity tracker = LRUCacheUtil.get(taskId);
            try {
                if (tracker != null) {
                    tracker.setProgress(50);
                    LRUCacheUtil.put(taskId, tracker);
                }
                // TODO 2025/7/7 RAG 上传
                vectorDbService.addFileVectors(this.file, metadatas, category);

                if (tracker != null) {
                    tracker.setProgress(70);
                    LRUCacheUtil.put(taskId, tracker);
                }

                UploadFile entity = new UploadFile();
                entity.setCategory(category);
                entity.setFilename(filename);
                entity.setFilepath(filepath);
                entity.setFileId(fileId);
                entity.setUserId(userId);
                entity.setCreateTime(new Date().getTime());
                if(knowledgeBase != null) {
                    entity.setKnowledgeBaseId(knowledgeBase.getId());
                }
                uploadFileService.addUploadFile(entity);

                if (tracker != null) {
                    tracker.setProgress(100);
                    LRUCacheUtil.put(taskId, tracker);
                }
            } catch (IOException | SQLException e) {
                tracker.setProgress(-1);
                LRUCacheUtil.put(taskId, tracker);
                e.printStackTrace();
            }
        }
    }

    protected String requestToJson(HttpServletRequest request) throws IOException {
        InputStream in = request.getInputStream();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len;
        while ((len = in.read(buffer)) != -1) {
            out.write(buffer, 0, len);
        }
        out.close();
        in.close();
        return new String(out.toByteArray(), StandardCharsets.UTF_8);
    }
}

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
import ai.medusa.MedusaService;
import ai.medusa.pojo.InstructionData;
import ai.medusa.pojo.InstructionPairRequest;
import ai.migrate.service.UploadFileService;
import ai.utils.ExcelSqlUtil;
import ai.vector.VectorCacheLoader;
import ai.vector.VectorStoreService;
import ai.vector.pojo.UpsertRecord;
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
        }
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
        List<String> idList = gson.fromJson(requestToJson(req), new TypeToken<List<String>>() {
        }.getType());
        vectorDbService.deleteDoc(idList);
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

        if (req.getParameter("pageNumber") != null) {
            pageSize = Integer.parseInt(req.getParameter("pageSize"));
            pageNumber = Integer.parseInt(req.getParameter("pageNumber"));
        }

        Map<String, Object> map = new HashMap<>();
        List<UploadFile> result = null;
        int totalRow = 0;
        try {
            result = uploadFileService.getUploadFileList(pageNumber, pageSize, category);
            totalRow = uploadFileService.getTotalRow(category);
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

    private void uploadLearningFile(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession();
        String category = req.getParameter("category");
        String level = req.getParameter("level");
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
        List<Future<?>> futures = new ArrayList<>();
        if (!files.isEmpty()) {
            JsonArray fileList = new JsonArray();
            for (File file : files) {
                if (file.exists() && file.isFile()) {
                    String filename = realNameMap.get(file.getName());
                    Future<?> future =uploadExecutorService.submit(new AddDocIndex(file, category, filename, level));
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
            PrintWriter out = resp.getWriter();
            out.write(gson.toJson(jsonResult));
            out.flush();
            out.close();

    }


    public class AddDocIndex extends Thread {
        private final VectorDbService vectorDbService = new VectorDbService(config);
        private final File file;
        private final String category;
        private final String filename;
        private final String level;

        public AddDocIndex(File file, String category, String filename, String level) {
            this.file = file;
            this.category = category;
            this.filename = filename;
            this.level = level;
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
            if (level == null) {
                metadatas.put("level", "user");
            } else {
                metadatas.put("level", level);
            }

            try {
                vectorDbService.addFileVectors(this.file, metadatas, category);
                UploadFile entity = new UploadFile();
                entity.setCategory(category);
                entity.setFilename(filename);
                entity.setFilepath(filepath);
                entity.setFileId(fileId);
                uploadFileService.addUploadFile(entity);
            } catch (IOException | SQLException e) {
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

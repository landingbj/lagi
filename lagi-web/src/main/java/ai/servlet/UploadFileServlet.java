package ai.servlet;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import ai.servlet.dto.CompanyIncomingDocumentsRequest;
import ai.vector.pojo.VectorCollection;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import ai.common.pojo.*;
import ai.config.ContextLoader;
import ai.medusa.MedusaService;
import ai.medusa.pojo.InstructionData;
import ai.medusa.pojo.InstructionPairRequest;
import ai.medusa.pojo.InstructionPairRequestContractedHotels;
import ai.migrate.service.UploadFileService;
import ai.servlet.dto.MeetingMinutesRequest;
import ai.vector.VectorCacheLoader;
import ai.vector.VectorStoreService;
import ai.embedding.Embeddings;
import ai.embedding.impl.TelecomGteEmbeddings;
import ai.vector.impl.ChromaVectorStore;
import ai.vector.pojo.IndexRecord;
import ai.vector.pojo.QueryCondition;
import ai.vector.pojo.UpsertRecord;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.lang3.StringUtils;

import ai.vector.FileService;
import ai.vector.VectorDbService;
import ai.utils.MigrateGlobal;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.junit.Test;

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
    private static final String UPLOAD_HYJY = "/upload/HYJY";
    private static final String UPLOAD_GSSW = "/upload/GSSW";

    private static final ExecutorService uploadExecutorService = Executors.newFixedThreadPool(1);
    private final Embeddings ef = new TelecomGteEmbeddings(ContextLoader.configuration.getFunctions().getEmbedding().get(0));
    private final ChromaVectorStore vectorStore = new ChromaVectorStore(ContextLoader.configuration.getStores().getVectors().get(0),ef);


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
        } else if (method.equals("pairingContractedHotels")) {
            this.pairingContractedHotels(req, resp);
        } else if (method.equals("pairingMeetingMinutes")) {
            this.pairingMeetingMinutes(req, resp);
        } else if (method.equals("uploadMeetingMinutes")) {
            this.uploadMeetingMinutes(req, resp);
        } else if (method.equals("deleteCategoryFile")) {
            this.deleteCategoryFile(req, resp);
        } else if (method.equals("getMeetingUploadFileList")) {
            this.getMeetingUploadFileList(req, resp);
        } else if (method.equals("permissionSettings")) {
            this.permissionSettings(req, resp);
        } else if (method.equals("permissionSettingsIncrement")) {
            this.permissionSettingsIncrement(req, resp);
        } else if (method.equals("uploadCompanyIncomingDocuments")) {
            this.uploadCompanyIncomingDocuments(req, resp);
        }
    }


    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doGet(req, resp);
    }
    private void uploadCompanyIncomingDocuments(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setHeader("Content-Type", "application/json;charset=utf-8");
        HttpSession session = req.getSession();
        CompanyIncomingDocumentsRequest crequest = new CompanyIncomingDocumentsRequest();

        Field[] fields = CompanyIncomingDocumentsRequest.class.getDeclaredFields();

        JsonObject jsonResult = new JsonObject();

        for (Field field : fields) {
            String fieldName = field.getName();
            String parameterValue = req.getParameter(fieldName);
            if (parameterValue != null && !parameterValue.isEmpty()) {
                field.setAccessible(true);
                try {
                    if (field.getType().equals(Date.class)) {
                        // 将字符串转为 Date 类型
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd"); // 请根据需要调整格式
                        Date dateValue = sdf.parse(parameterValue);
                        field.set(crequest, dateValue);
                    } else {
                        // 其他类型直接设置值
                        field.set(crequest, parameterValue);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("字段映射出错了---");
                }
            }
        }

        if (crequest.getId()==null || crequest.getId().isEmpty()){
            jsonResult.addProperty("status", "failed");
            jsonResult.addProperty("data", "The id is empty");
            PrintWriter out = resp.getWriter();
            out.write(gson.toJson(jsonResult));
            out.flush();
            out.close();
            return;
        }
        String level = crequest.getLevel();
        String fileId = crequest.getId();

        StringBuilder introduceBuilder = new StringBuilder();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        if (crequest.getSerialNumber() != null && !crequest.getSerialNumber().isEmpty()) {
            introduceBuilder.append("流水号为：").append(crequest.getSerialNumber()).append(",/n");
        }
        if (crequest.getReceiptDate() != null) {
            introduceBuilder.append("收文日期为：").append(dateFormat.format(crequest.getReceiptDate())).append(",/n");
        }
        if (crequest.getDocumentNumber() != null && !crequest.getDocumentNumber().isEmpty()) {
            introduceBuilder.append("来文字号为：").append(crequest.getDocumentNumber()).append(",/n");
        }
        if (crequest.getIssuingUnit() != null && !crequest.getIssuingUnit().isEmpty()) {
            introduceBuilder.append("来文单位：").append(crequest.getIssuingUnit()).append(",/n");
        }
        if (crequest.getDocumentTitle() != null && !crequest.getDocumentTitle().isEmpty()) {
            introduceBuilder.append("标题为：").append(crequest.getDocumentTitle()).append(",/n");
        }

        introduceBuilder.append(";/n");
        String introduce = introduceBuilder.toString();
        List<Map<String, String>> data = new ArrayList<>();
        jsonResult.addProperty("status", "success");

        DiskFileItemFactory factory = new DiskFileItemFactory();
        ServletFileUpload upload = new ServletFileUpload(factory);
        upload.setFileSizeMax(MigrateGlobal.DOC_FILE_SIZE_LIMIT);
        upload.setSizeMax(MigrateGlobal.DOC_FILE_SIZE_LIMIT);
        String uploadDir = getServletContext().getRealPath(UPLOAD_GSSW);
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

                    String newName = fileId + fileName.substring(fileName.lastIndexOf("."));
                    String lastFilePath = uploadDir + File.separator + newName;
                    File file = new File(lastFilePath);

                    while (!file.exists()){
                        fi.write(file);
                    }
                    files.add(file);
                    realNameMap.put(file.getName(), fileName);
                    session.setAttribute(newName, file.toString());
                    session.setAttribute("lastFilePath", lastFilePath);
                }
            }
        } catch (Exception ex) {
            jsonResult.addProperty("msg", "There was an error parsing the file - Company incoming documents");
            ex.printStackTrace();
        }
        if (!files.isEmpty()) {
                String content = "";
                for (File file : files) {
                    content = fileService.getFileContent(file);
                    if (!StringUtils.isEmpty(content)) {
                        String filename = realNameMap.get(file.getName());
                        Map<String, String> map = new HashMap<>();
                        map.put("filename", filename);
                        map.put("filepath", file.getName());
                        data.add(map);
                        uploadExecutorService.submit(new AddMeetingIndex(file, "GSSW", filename, level, fileId,introduce));
                    }
                }
        }


        if (!jsonResult.has("msg")) {
            jsonResult.addProperty("data", gson.toJson(data));
            jsonResult.addProperty("status", "success");
        }
        PrintWriter out = resp.getWriter();
        out.write(gson.toJson(jsonResult));
        out.flush();
        out.close();
    }

    private void getMeetingUploadFileList(HttpServletRequest req, HttpServletResponse resp)  throws ServletException, IOException {
    resp.setHeader("Content-Type", "application/json;charset=utf-8");
        int pageSize = Integer.MAX_VALUE;
        int pageNumber = 1;

        String category = req.getParameter("category");
        String fileId = req.getParameter("fileId");

        if (req.getParameter("pageNumber") != null) {
            pageSize = Integer.parseInt(req.getParameter("pageSize"));
            pageNumber = Integer.parseInt(req.getParameter("pageNumber"));
        }

        Map<String, Object> map = new HashMap<>();
        List<UploadFile> result = null;
        int totalRow = 0;
        try {
            result = uploadFileService.getMeetingUploadFileList(pageNumber, pageSize, category);
            totalRow = uploadFileService.getMeetingTotalRow(category);
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

     private void pairingContractedHotels(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        InstructionPairRequestContractedHotels instructionPairRequest = mapper.readValue(requestToJson(req), InstructionPairRequestContractedHotels.class);
        long timestamp = Instant.now().toEpochMilli();
        new Thread(() -> {
            List<InstructionData> instructionDataList = instructionPairRequest.getData();
            String category = instructionPairRequest.getCategory();
            String level = Optional.ofNullable(instructionPairRequest.getLevel()).orElse("user");
            Map<String, String> qaMap = new HashMap<>();
            for (InstructionData data : instructionDataList) {
                if (data!=null){
                for (String instruction : data.getInstruction()) {
                    instruction = instruction.trim();
                    String output = data.getOutput().trim();
                    qaMap.put(instruction, output);
                    Map<String, String> metadata = new HashMap<>();
                    metadata.put("category", category);
                    metadata.put("level", level);
                    metadata.put("place", instructionPairRequest.getPlace());
                    metadata.put("type", instructionPairRequest.getType());
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
                }else{
                    System.out.println("导入的jons是"+"null");
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

     private void pairingMeetingMinutes(HttpServletRequest req, HttpServletResponse resp) throws IOException {
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
        Map<String, Object> map = new HashMap<>();
        map.put("status", "success");
        PrintWriter out = resp.getWriter();
        out.print(gson.toJson(map));
        out.flush();
        out.close();
    }

    private void deleteCategoryFile(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        req.setCharacterEncoding("utf-8");
        resp.setContentType("application/json;charset=utf-8");
        Map<String, Object> map = new HashMap<>();

        Map<String, Object> map1 = parseJsonToMap(req);
        if (map1.get("category")!=null&&map1.get("idList")!=null){
             List<String> idList = (List<String>) map1.get("idList");
             String category = map1.get("category").toString();
             String[] categorys = stringSplitter(category);
            for (String s : categorys) {
             vectorDbService.deleteDoc(idList,s);
             uploadFileService.deleteCategoryFile(idList,s);
            }
             map.put("status", "success");
        }else if (map1.get("category")==null&&map1.get("idList")!=null){
            List<String> idList = (List<String>) map1.get("idList");
             vectorDbService.deleteDoc(idList);
             uploadFileService.deleteCategoryFile(idList,null);
             map.put("status", "success");
        }else {
            map.put("status", "failed");
            map.put("data", "请检测请求参数！");
        }
        PrintWriter out = resp.getWriter();
        out.print(gson.toJson(map));
        out.flush();
        out.close();
    }

    public void permissionSettings(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setHeader("Content-Type", "application/json;charset=utf-8");
        Map<String, Object> map = new HashMap<>();
        String authorizer = req.getParameter("authorizer");
        String fileId = req.getParameter("fileId");
        if (authorizer!=null&&fileId!=null){
            String[] categorys = stringSplitter(authorizer);
            QueryCondition queryCondition = new QueryCondition();
                Map<String, String> where = new HashMap<>();
                where.put("file_id", fileId);
                queryCondition.setWhere(where);

            //查
            List<IndexRecord> indexRecords = new ArrayList<>();
            String filename = "";
            String filepath = "";
             List<String> permissions = new ArrayList<>();
             permissions.addAll(uploadFileService.getMeetingPermissions(fileId));
            for (String permission : permissions) {
                  if (permissions.size()>0){
                      indexRecords = vectorStoreService.query(queryCondition,permission);
                        if (indexRecords.size()>0){
                        filename = indexRecords.get(0).getMetadata().get("filename").toString();
                        filepath = indexRecords.get(0).getMetadata().get("filepath").toString();
                        break;
                        }
                  }
            }

            if(indexRecords.size()>0){
                    //删
                    List<Map<String, String>> whereList = new ArrayList<>();
                        whereList.add(where);
                    vectorStore.deleteWhere(whereList);
                    List<String> idList = new ArrayList<>();
                    idList.add(fileId);
                     for (String permission : permissions) {
                      uploadFileService.deleteCategoryFile(idList,permission);
                    }
                     //增
                    for (String category : categorys) {
                        for (IndexRecord indexRecord : indexRecords) {
                        UpsertRecord upsertRecord = new UpsertRecord();
                        upsertRecord.setDocument(indexRecord.getDocument());
                        upsertRecord.setId(indexRecord.getId());
                        Map<String, String> convertedMap = indexRecord.getMetadata().entrySet().stream()
                        .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            entry -> String.valueOf(entry.getValue())
                        ));
                        upsertRecord.setMetadata(convertedMap);

                        List<UpsertRecord> upsertRecords = new ArrayList<>();
                        upsertRecords.add(upsertRecord);
                        vectorStore.upsert(upsertRecords, category);
                        }
                        try {
                        UploadFile entity = new UploadFile();
                        entity.setCategory(category);
                        entity.setFilename(filename);
                        entity.setFilepath(filepath);
                        entity.setFileId(fileId);
                        uploadFileService.addUploadMeetingFile(entity);
                        }catch (Exception e){
                             map.put("status", "failed");
                             map.put("data", "请检测请求参数！");
                            break;
                        }
                    }
                    map.put("status", "success");
                    map.put("data", "权限更新成功！");
                }else {
                    map.put("status", "failed");
                    map.put("data", "向量数据库，未找到该文件！");
                }
            }else {
             map.put("status", "failed");
             map.put("data", "请检测请求参数！");
            }


        PrintWriter out = resp.getWriter();
        out.print(gson.toJson(map));
        out.flush();
        out.close();

    }

     public void permissionSettingsIncrement(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setHeader("Content-Type", "application/json;charset=utf-8");
        Map<String, Object> map = new HashMap<>();
        String authorizer = req.getParameter("authorizer");
        String fileId = req.getParameter("fileId");
        if (authorizer!=null&&fileId!=null){
            String[] categorys = stringSplitter(authorizer);
            QueryCondition queryCondition = new QueryCondition();
                Map<String, String> where = new HashMap<>();
                where.put("file_id", fileId);
                queryCondition.setWhere(where);

            //查
            List<IndexRecord> indexRecords = new ArrayList<>();
            String filename = "";
            String filepath = "";
            for (VectorCollection collection: vectorStoreService.listCollections()) {
                indexRecords = vectorStoreService.query(queryCondition,collection.getCategory());
                if (indexRecords.size()>0){
                filename = indexRecords.get(0).getMetadata().get("filename").toString();
                filepath = indexRecords.get(0).getMetadata().get("filepath").toString();
                break;
                }
            }
            if(indexRecords.size()>0){
                     //增
                 Integer row = -1;
                    for (String category : categorys) {
                     try {
                        row = uploadFileService.getMeetingFileIdTotalRow(category,fileId);
                        }catch (SQLException e){

                        }
                     if (row<=0){
                         for (IndexRecord indexRecord : indexRecords) {
                        UpsertRecord upsertRecord = new UpsertRecord();
                        upsertRecord.setDocument(indexRecord.getDocument());
                        upsertRecord.setId(indexRecord.getId());
                        Map<String, String> convertedMap = indexRecord.getMetadata().entrySet().stream()
                        .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            entry -> String.valueOf(entry.getValue())
                        ));
                        upsertRecord.setMetadata(convertedMap);

                        List<UpsertRecord> upsertRecords = new ArrayList<>();
                        upsertRecords.add(upsertRecord);
                        vectorStore.upsert(upsertRecords, category);
                        }
                        try {
                        UploadFile entity = new UploadFile();
                        entity.setCategory(category);
                        entity.setFilename(filename);
                        entity.setFilepath(filepath);
                        entity.setFileId(fileId);
                        uploadFileService.addUploadMeetingFile(entity);
                        }catch (Exception e){
                             map.put("status", "failed");
                             map.put("data", "请检测请求参数！");
                            break;
                        }
                     }

                    }
                    map.put("status", "success");
                    map.put("data", "权限更新成功！");
                }else {
                    map.put("status", "failed");
                    map.put("data", "向量数据库，未找到该文件！");
                }
            }else {
             map.put("status", "failed");
             map.put("data", "请检测请求参数！");
            }


        PrintWriter out = resp.getWriter();
        out.print(gson.toJson(map));
        out.flush();
        out.close();

    }

     public static Map<String, Object> parseJsonToMap(HttpServletRequest req) throws IOException {
        StringBuilder jsonBuilder = new StringBuilder();
        BufferedReader reader = req.getReader();
        String line;
        while ((line = reader.readLine()) != null) {
            jsonBuilder.append(line);
        }
        String json = jsonBuilder.toString();
        Gson gson = new Gson();
        Type type = new TypeToken<Map<String, Object>>() {}.getType();
        Map<String, Object> map = gson.fromJson(json, type);
        if (map.containsKey("category")) {
            Object hobbiesObj = map.get("category");
            if (hobbiesObj instanceof List) {
                List<String> hobbies = (List<String>) hobbiesObj;
                //System.out.println("fileIds: " + hobbies);
            }
        }
        return map;
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
        String type = req.getParameter("type");
        // 设置响应内容类型
        resp.setContentType("application/pdf");
        String encodedFileName = URLEncoder.encode(fileName, "UTF-8");
        resp.setHeader("Content-Disposition", "attachment; filename=\"" + encodedFileName + "\"");
        String uploadDir = "";
        if (type==null){
             uploadDir = getServletContext().getRealPath(UPLOAD_DIR);
        }else if(type.equals("HYJY")) {
             uploadDir = getServletContext().getRealPath(UPLOAD_HYJY);
        }else if(type.equals("GSSW")){
            uploadDir = getServletContext().getRealPath(UPLOAD_GSSW);
        }

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

    private void uploadMeetingMinutes(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setHeader("Content-Type", "application/json;charset=utf-8");
        HttpSession session = req.getSession();

        MeetingMinutesRequest meetingMinutesRequest = new MeetingMinutesRequest();

        Field[] fields = MeetingMinutesRequest.class.getDeclaredFields();

        JsonObject jsonResult = new JsonObject();

        for (Field field : fields) {
            String fieldName = field.getName();
            String parameterValue = req.getParameter(fieldName);
            if (parameterValue != null && !parameterValue.isEmpty()) {
                field.setAccessible(true);
                try {
                    if (field.getType().equals(Date.class)) {
                        // 将字符串转为 Date 类型
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // 请根据需要调整格式
                        Date dateValue = sdf.parse(parameterValue);
                        field.set(meetingMinutesRequest, dateValue);
                    } else {
                        // 其他类型直接设置值
                        field.set(meetingMinutesRequest, parameterValue);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("字段映射出错了---");
                }
            }
        }

        String[] categorys = stringSplitter(meetingMinutesRequest.getAuthorizer());
        if (categorys.length <= 0 || meetingMinutesRequest.getId()==null || meetingMinutesRequest.getId().isEmpty()){
            jsonResult.addProperty("status", "failed");
            jsonResult.addProperty("data", "The authorizer or id is empty");
            PrintWriter out = resp.getWriter();
            out.write(gson.toJson(jsonResult));
            out.flush();
            out.close();
            return;
        }
        String level = meetingMinutesRequest.getLevel();
        String fileId = meetingMinutesRequest.getId();


        StringBuilder introduceBuilder = new StringBuilder();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat dateFormats = new SimpleDateFormat("yyyy-MM-dd");

        // 检查并添加每个字段，如果字段不为空
        if (meetingMinutesRequest.getNumber() != null && !meetingMinutesRequest.getNumber().isEmpty()) {
            introduceBuilder.append("编号为：").append(meetingMinutesRequest.getNumber()).append("，");
        }
        if (meetingMinutesRequest.getMeetingCategory() != null && !meetingMinutesRequest.getMeetingCategory().isEmpty()) {
            introduceBuilder.append("会议类别为：").append(meetingMinutesRequest.getMeetingCategory()).append("，");
        }
        if (meetingMinutesRequest.getTitle() != null && !meetingMinutesRequest.getTitle().isEmpty()) {
            introduceBuilder.append("主题为：").append(meetingMinutesRequest.getTitle()).append("，");
        }
        if (meetingMinutesRequest.getDrafter() != null && !meetingMinutesRequest.getDrafter().isEmpty()) {
            introduceBuilder.append("拟稿人为：").append(meetingMinutesRequest.getDrafter()).append("，");
        }
        if (meetingMinutesRequest.getMeetingPlace() != null && !meetingMinutesRequest.getMeetingPlace().isEmpty()) {
            introduceBuilder.append("会议地点为：").append(meetingMinutesRequest.getMeetingPlace()).append("，");
        }
        if (meetingMinutesRequest.getHostingUnit() != null && !meetingMinutesRequest.getHostingUnit().isEmpty()) {
            introduceBuilder.append("会议承办单位为：").append(meetingMinutesRequest.getHostingUnit()).append("，");
        }
        if (meetingMinutesRequest.getMeetingTime() != null) {
            Date issueDate = meetingMinutesRequest.getMeetingTime();
            String msg = dateFormat.format(issueDate);
            introduceBuilder.append("会议时间为：").append(msg).append("，");
        }
        if (meetingMinutesRequest.getPhone() != null && !meetingMinutesRequest.getPhone().isEmpty()) {
            introduceBuilder.append("电话为：").append(meetingMinutesRequest.getPhone()).append("，");
        }
        if (meetingMinutesRequest.getConfidentiality() != null && !meetingMinutesRequest.getConfidentiality().isEmpty()) {
            introduceBuilder.append("密级为：").append(meetingMinutesRequest.getConfidentiality()).append("，");
        }
        if (meetingMinutesRequest.getCopyNumber() != null && !meetingMinutesRequest.getCopyNumber().isEmpty()) {
            introduceBuilder.append("份号为：").append(meetingMinutesRequest.getCopyNumber()).append("，");
        }
        if (meetingMinutesRequest.getDistribution() != null && !meetingMinutesRequest.getDistribution().isEmpty()) {
            introduceBuilder.append("分送单位为：").append(meetingMinutesRequest.getDistribution()).append("，");
        }
        if (meetingMinutesRequest.getIssueDate() != null ) {
            Date issueDate = meetingMinutesRequest.getIssueDate();
             String formattedDate = dateFormats.format(issueDate);
            introduceBuilder.append("印发日期为：").append(formattedDate).append("，");
        }
        if (meetingMinutesRequest.getUrgency() != null && !meetingMinutesRequest.getUrgency().isEmpty()) {
            introduceBuilder.append("缓急为：").append(meetingMinutesRequest.getUrgency()).append("，");
        }
        if (meetingMinutesRequest.getContent() != null && !meetingMinutesRequest.getContent().isEmpty()) {
            introduceBuilder.append("正文链接为：").append(meetingMinutesRequest.getContent()).append("。");
        }
        introduceBuilder.append(";/n");
        String introduce = introduceBuilder.toString();
        List<Map<String, String>> data = new ArrayList<>();
        jsonResult.addProperty("status", "success");

        DiskFileItemFactory factory = new DiskFileItemFactory();
        ServletFileUpload upload = new ServletFileUpload(factory);
        upload.setFileSizeMax(MigrateGlobal.DOC_FILE_SIZE_LIMIT);
        upload.setSizeMax(MigrateGlobal.DOC_FILE_SIZE_LIMIT);
        String uploadDir = getServletContext().getRealPath(UPLOAD_HYJY);
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

                            String newName = fileId + fileName.substring(fileName.lastIndexOf("."));
                            String lastFilePath = uploadDir + File.separator + newName;
                            File file = new File(lastFilePath);

                            while (!file.exists()){
                                fi.write(file);
                            }
                            files.add(file);
                            realNameMap.put(file.getName(), fileName);
                            session.setAttribute(newName, file.toString());
                            session.setAttribute("lastFilePath", lastFilePath);
                        }
                    }
                } catch (Exception ex) {
                    jsonResult.addProperty("msg", "There was an error parsing the file - meeting minutes");
                    ex.printStackTrace();
                }
        for (String category : categorys) {
            if (!files.isEmpty()) {
                String content = "";
                for (File file : files) {
                    content = fileService.getFileContent(file);
                    if (!StringUtils.isEmpty(content)) {
                        String filename = realNameMap.get(file.getName());
                        Map<String, String> map = new HashMap<>();
                        map.put("filename", filename);
                        map.put("filepath", file.getName());
                        data.add(map);
                        uploadExecutorService.submit(new AddMeetingIndex(file, category, filename, level, fileId,introduce));
                    }
                }
            }

        }
          if (!jsonResult.has("msg")) {
                jsonResult.addProperty("data", gson.toJson(data));
                jsonResult.addProperty("status", "success");
            }
        PrintWriter out = resp.getWriter();
        out.write(gson.toJson(jsonResult));
        out.flush();
        out.close();
    }

    private String[] stringSplitter(String authorizer) {
        String formattedString = authorizer.replace('，', ',');
               formattedString = formattedString.replaceAll(",$", "");
        String[] splitStrings = formattedString.split(",");
         Set<String> set = new HashSet<>(Arrays.asList(splitStrings));
        String[] uniqueStrings = set.toArray(new String[0]);
        return uniqueStrings;

    }

    @Test
    public  void ttt() {
         String[] aaa=  stringSplitter("A03161,A00359,A02187,A03451,A02187,A03161");
        for (String s : aaa) {
             System.out.println(s);
        }

    }

    private void uploadLearningFile(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession();
        String category = req.getParameter("category");
        String level = req.getParameter("level");
        String fileId = req.getParameter("fileId");
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

        List<Map<String, String>> data = new ArrayList<>();
        if (!files.isEmpty()) {
            String content = "";
            for (File file : files) {
                content = fileService.getFileContent(file);
                if (!StringUtils.isEmpty(content)) {
                    String filename = realNameMap.get(file.getName());
                    Map<String, String> map = new HashMap<>();
                    map.put("filename", filename);
                    map.put("filepath", file.getName());
                    data.add(map);
                    uploadExecutorService.submit(new AddDocIndex(file, category, filename, level, fileId));
                }
            }
        }
        if (!jsonResult.has("msg")) {
            jsonResult.addProperty("data", gson.toJson(data));
            jsonResult.addProperty("status", "success");
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
        private String fileId;

        public AddDocIndex(File file, String category, String filename, String level, String fileId) {
            this.file = file;
            this.category = category;
            this.filename = filename;
            this.level = level;
            this.fileId = fileId;
        }

        public void run() {
            if (vectorDbService.vectorStoreEnabled()) {
                addDocIndexes();
            }
        }

        private void addDocIndexes() {
            Map<String, Object> metadatas = new HashMap<>();
            if (fileId == null) {
                fileId = UUID.randomUUID().toString().replace("\\", "");
            }
            //String filepath = MEETING_PATH+file.getName();
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

    public class AddMeetingIndex extends Thread {
        private final VectorDbService vectorDbService = new VectorDbService(config);
        private final File file;
        private final String category;
        private final String filename;
        private final String level;
        private String fileId;
        private String title;

        public AddMeetingIndex(File file, String category, String filename, String level, String fileId, String title) {
            this.file = file;
            this.category = category;
            this.filename = filename;
            this.level = level;
            this.fileId = fileId;
            this.title = title;
        }

        public void run() {
            if (vectorDbService.vectorStoreEnabled()) {
                addMeetingIndexes();
            }
        }

        private void addMeetingIndexes() {
            Map<String, Object> metadatas = new HashMap<>();
            if (fileId == null) {
                fileId = UUID.randomUUID().toString().replace("\\", "");
            }
            String filepath = file.getName();

            metadatas.put("filename", filename);
            metadatas.put("category", category);
            metadatas.put("filepath", filepath);
            metadatas.put("file_id", fileId);
            metadatas.put("title", title);

            if (level == null) {
                metadatas.put("level", "user");
            } else {
                metadatas.put("level", level);
            }

            try {
                vectorDbService.addFileVectors(this.file, metadatas, category,title);
                UploadFile entity = new UploadFile();
                entity.setCategory(category);
                entity.setFilename(filename);
                entity.setFilepath(filepath);
                entity.setFileId(fileId);
                uploadFileService.addUploadMeetingFile(entity);
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

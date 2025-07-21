package ai.servlet;

import lombok.*;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@MultipartConfig(
        fileSizeThreshold = 1024 * 1024 * 2, // 2MB
        maxFileSize = 1024 * 1024 * 1024,      // 1GB
        maxRequestSize = 1024 * 1024 * 1024 * 10 // 10GB
)
//public class ModelUploadServlet extends RestfulServlet{
public class ModelUploadServlet extends BaseServlet{


    // 存储临时文件的目录
    private static final String UPLOAD_DIR = "uploads";
    // 存储文件上传信息的Map
    private static final Map<String, FileInfo> fileInfoMap = new ConcurrentHashMap<>();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        // 处理文件分片上传
        String requestURI = request.getRequestURI();
        if (request.getRequestURI().endsWith("/merge")) {
            handleMerge(request, response);
            return;
        }

        request.setCharacterEncoding("UTF-8");

        // 获取上传的文件分片
        Part filePart = request.getPart("file");
        if (filePart == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing file part");
            return;
        }

        // 获取参数
        String fileName = request.getParameter("fileName");
        String fileId = request.getParameter("fileId");
        int chunkIndex = Integer.parseInt(request.getParameter("chunkIndex"));
        int totalChunks = Integer.parseInt(request.getParameter("totalChunks"));
        long chunkSize = Long.parseLong(request.getParameter("chunkSize"));
        long totalSize = Long.parseLong(request.getParameter("totalSize"));

        // 创建上传目录（如果不存在）
        File uploadDir = new File(UPLOAD_DIR);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        // 创建文件临时目录
        File tempDir = new File(uploadDir, fileId);
        if (!tempDir.exists()) {
            tempDir.mkdirs();
        }

        // 保存分片文件
        File chunkFile = new File(tempDir, "part" + chunkIndex);
        try (InputStream inputStream = filePart.getInputStream();
             OutputStream outputStream = new FileOutputStream(chunkFile)) {

            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        }

        // 更新文件信息
        FileInfo fileInfo = fileInfoMap.computeIfAbsent(fileId,
                k -> new FileInfo(fileName, totalSize, totalChunks));
        fileInfo.addUploadedChunk(chunkIndex);

        // 返回成功响应
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        try (PrintWriter out = response.getWriter()) {
            out.println("{\"status\": \"success\", \"chunkIndex\": " + chunkIndex + "}");
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        // 检查文件上传状态
        String fileId = request.getParameter("fileId");
        if (fileId == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing fileId parameter");
            return;
        }

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // 检查文件是否存在
        File tempDir = new File(UPLOAD_DIR, fileId);
        if (!tempDir.exists() || !tempDir.isDirectory()) {
            try (PrintWriter out = response.getWriter()) {
                out.println("{\"completed\": false, \"uploadedChunks\": []}");
            }
            return;
        }

        // 获取已上传的分片
        File[] chunkFiles = tempDir.listFiles((dir, name) -> name.startsWith("part"));
        if (chunkFiles == null || chunkFiles.length == 0) {
            try (PrintWriter out = response.getWriter()) {
                out.println("{\"completed\": false, \"uploadedChunks\": []}");
            }
            return;
        }

        // 提取分片索引
        List<Integer> uploadedChunks = new ArrayList<>();
        for (File chunk : chunkFiles) {
            try {
                int index = Integer.parseInt(chunk.getName().substring(4));
                uploadedChunks.add(index);
            } catch (NumberFormatException e) {
                // 忽略格式不正确的文件
            }
        }

        // 检查是否所有分片都已上传
        FileInfo fileInfo = fileInfoMap.get(fileId);
        boolean completed = fileInfo != null &&
                fileInfo.getUploadedChunks().size() == fileInfo.getTotalChunks();

        // 返回结果
        try (PrintWriter out = response.getWriter()) {
            out.print("{\"completed\": " + completed + ", \"uploadedChunks\": [");
            for (int i = 0; i < uploadedChunks.size(); i++) {
                out.print(uploadedChunks.get(i));
                if (i < uploadedChunks.size() - 1) {
                    out.print(",");
                }
            }
            out.println("]}");
        }
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @ToString
    @Builder
    static class FileRequest {
        private String fileId;
        private String fileName;
        private int totalChunks;
        private long totalSize;
    }

    // 处理合并请求
    private void handleMerge(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        try (BufferedReader reader = request.getReader()) {
            StringBuilder jsonBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonBuilder.append(line);
            }

            // 解析JSON请求
            String json = jsonBuilder.toString();
            FileRequest fileRequest = gson.fromJson(json, FileRequest.class);
            Map<String, Object> requestData = parseJson(json);

            String fileId = fileRequest.getFileId();
            String fileName = fileRequest.getFileName();
            int totalChunks = fileRequest.getTotalChunks();
            long totalSize = fileRequest.getTotalSize() ;

            // 合并文件
            mergeChunks(fileId, fileName, totalChunks, totalSize, response);
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to merge file");
        }
    }

    // 简单的JSON解析方法
    private Map<String, Object> parseJson(String json) {
        Map<String, Object> result = new HashMap<>();
        json = json.trim();
        if (json.startsWith("{") && json.endsWith("}")) {
            json = json.substring(1, json.length() - 1);
            String[] keyValues = json.split(",");
            for (String keyValue : keyValues) {
                String[] parts = keyValue.split(":", 2);
                if (parts.length == 2) {
                    String key = parts[0].trim().replaceAll("\"", "");
                    String value = parts[1].trim();

                    if (value.startsWith("\"") && value.endsWith("\"")) {
                        result.put(key, value.substring(1, value.length() - 1));
                    } else if (value.equals("true") || value.equals("false")) {
                        result.put(key, Boolean.parseBoolean(value));
                    } else {
                        try {
                            // 尝试解析为数字
                            if (value.contains(".")) {
                                result.put(key, Double.parseDouble(value));
                            } else {
                                long longValue = Long.parseLong(value);
                                if (longValue <= Integer.MAX_VALUE) {
                                    result.put(key, (int) longValue);
                                } else {
                                    result.put(key, longValue);
                                }
                            }
                        } catch (NumberFormatException e) {
                            result.put(key, value);
                        }
                    }
                }
            }
        }
        return result;
    }

    // 合并文件分片
    private void mergeChunks(String fileId, String fileName, int totalChunks, long totalSize,
                             HttpServletResponse response) throws IOException {
        File tempDir = new File(UPLOAD_DIR, fileId);
        File finalFile = new File(UPLOAD_DIR, fileName);

        // 创建父目录（如果不存在）
        if (!finalFile.getParentFile().exists()) {
            finalFile.getParentFile().mkdirs();
        }

        try (RandomAccessFile raf = new RandomAccessFile(finalFile, "rw")) {
            // 合并所有分片
            for (int i = 0; i < totalChunks; i++) {
                File chunkFile = new File(tempDir, "part" + i);
                if (!chunkFile.exists()) {
                    throw new IOException("Missing chunk: " + i);
                }

                try (FileInputStream fis = new FileInputStream(chunkFile)) {
                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    while ((bytesRead = fis.read(buffer)) != -1) {
                        raf.write(buffer, 0, bytesRead);
                    }
                }
            }
        }

        // 验证文件大小
        if (finalFile.length() != totalSize) {
            finalFile.delete();
            throw new IOException("File size mismatch after merging: " +
                    finalFile.length() + " vs " + totalSize);
        }

        // 删除临时目录
        deleteDirectory(tempDir);

        // 从内存中移除文件信息
        fileInfoMap.remove(fileId);

        // 返回成功响应
        response.setContentType("application/json");
        try (PrintWriter out = response.getWriter()) {
            out.println("{\"status\": \"success\", \"fileName\": \"" +
                    escapeJson(fileName) + "\", \"fileSize\": " + totalSize + "}");
        }
    }

    // 递归删除目录
    private void deleteDirectory(File directory) {
        if (directory.exists()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteDirectory(file);
                    } else {
                        file.delete();
                    }
                }
            }
            directory.delete();
        }
    }

    // 转义JSON字符串
    private String escapeJson(String input) {
        if (input == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            switch (c) {
                case '"':
                    sb.append("\\\"");
                    break;
                case '\\':
                    sb.append("\\\\");
                    break;
                case '\b':
                    sb.append("\\b");
                    break;
                case '\f':
                    sb.append("\\f");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                default:
                    if (c < ' ' || c > '~') {
                        String hex = Integer.toHexString(c);
                        sb.append("\\u");
                        for (int j = 0; j < 4 - hex.length(); j++) {
                            sb.append('0');
                        }
                        sb.append(hex.toUpperCase());
                    } else {
                        sb.append(c);
                    }
            }
        }
        return sb.toString();
    }

    // 文件信息类
    private static class FileInfo {
        private final String fileName;
        private final long totalSize;
        private final int totalChunks;
        private final Set<Integer> uploadedChunks = Collections.synchronizedSet(new HashSet<>());

        public FileInfo(String fileName, long totalSize, int totalChunks) {
            this.fileName = fileName;
            this.totalSize = totalSize;
            this.totalChunks = totalChunks;
        }

        public void addUploadedChunk(int chunkIndex) {
            uploadedChunks.add(chunkIndex);
        }

        public Set<Integer> getUploadedChunks() {
            return uploadedChunks;
        }

        public int getTotalChunks() {
            return totalChunks;
        }

        public boolean isComplete() {
            return uploadedChunks.size() == totalChunks;
        }
    }



}

package ai.servlet.api;

import ai.llm.service.CompletionsService;
import ai.openai.pojo.ChatCompletionRequest;
import ai.openai.pojo.ChatCompletionResult;
import ai.openai.pojo.ChatMessage;
import ai.servlet.BaseServlet;
import com.google.common.collect.Lists;
import org.apache.poi.xwpf.usermodel.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class PadCoordinateApiServlet extends BaseServlet {
    private static final String UPLOAD_DIR = "/upload";
    private static final String VLIMG_SUBDIR = "/vlimg";
    private static final String BASE_URL = "https://lumissil.saasai.top";

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setHeader("Content-Type", "application/json;charset=utf-8");
        String url = req.getRequestURI();
        String method = url.substring(url.lastIndexOf("/") + 1);

        if (method.equals("generatePadTable")) {
            this.generatePadTable(req, resp);
        } else if (method.equals("uploadImage")) {
            this.uploadImage(req, resp);
        } else {
            resp.getWriter().write("{\"status\": \"failed\", \"msg\": \"Unknown method\"}");
        }
    }
    private void uploadImage(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String msg = null;
        String imageUrl = null;

        try {
            List<File> files = getUploadFile(req, 10 * 1024 * 1024, 10 * 1024 * 1024, UPLOAD_DIR + VLIMG_SUBDIR);
            if (!files.isEmpty()) {
                File file = files.get(0);
                imageUrl = uploadFileAndGetUrl(file);
                resp.getWriter().write("{\"status\": \"success\", \"url\": \"" + imageUrl + "\"}");
            } else {
                msg = "未找到上传的文件";
                resp.getWriter().write("{\"status\": \"failed\", \"msg\": \"" + msg + "\"}");
            }
        } catch (Exception e) {
            msg = "上传文件失败: " + e.getMessage();
            e.printStackTrace();
            resp.getWriter().write("{\"status\": \"failed\", \"msg\": \"" + msg + "\"}");
        }
    }

    private String uploadFileAndGetUrl(File file) throws IOException {
        // 获取 Web 应用的根目录
        String webappDir = getServletContext().getRealPath("/");
        String imageDir = webappDir + UPLOAD_DIR + VLIMG_SUBDIR;

        // 确保目录存在
        File dir = new File(imageDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // 生成唯一文件名，防止覆盖
        String originalName = file.getName();
        String extension = originalName.substring(originalName.lastIndexOf("."));
        String uniqueName = UUID.randomUUID().toString() + extension;
        File destFile = new File(imageDir, uniqueName);

        // 移动文件到目标目录
        if (file.renameTo(destFile)) {
            // 生成公开 URL
            String relativePath = UPLOAD_DIR+ "/vlimg/" + uniqueName;
            return BASE_URL + relativePath;
        } else {
            throw new IOException("文件移动失败: " + file.getAbsolutePath());
        }
    }

    private void generatePadTable(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String msg = null;
        List<File> files = new ArrayList<>();

        try {
            files = getUploadFile(req, 10 * 1024 * 1024, 10 * 1024 * 1024, UPLOAD_DIR);
        } catch (Exception e) {
            msg = "解析文件出现错误";
            e.printStackTrace();
        }

        if (!files.isEmpty()) {
            File file = files.get(0);
            try {
                String fileContent = readFile(file);
                int expectedRows = countValidRows(fileContent);

                String tableContent = generateTableWithAI(fileContent, expectedRows);
                int actualRows = countTableRows(tableContent);
                if (actualRows < expectedRows) {
                    System.err.println("警告：生成表格行数(" + actualRows + ")少于预期行数(" + expectedRows + ")");
                    tableContent = generateFallbackTable(fileContent); // 使用后备方案
                }

                File wordFile = generateWordFile(tableContent);
                sendFile(resp, wordFile);

            } catch (Exception e) {
                e.printStackTrace();
                resp.getWriter().write("{\"status\": \"failed\", \"msg\": \"生成文件失败\"}");
            }
        } else {
            resp.getWriter().write("{\"status\": \"failed\", \"msg\": \"" + msg + "\"}");
        }
    }

    private String readFile(File file) throws IOException {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        }
        return content.toString();
    }

    private int countValidRows(String fileContent) {
        String[] lines = fileContent.split("\n");
        int validRows = 0;
        boolean dataSectionStarted = false;
        for (String line : lines) {
            line = line.trim();
            if (line.startsWith("Pad Opening Name")) {
                dataSectionStarted = true; // 数据部分开始
                continue;
            }
            if (!dataSectionStarted || line.isEmpty() || line.startsWith("*") || line.startsWith("#")) {
                continue; // 跳过头部和注释
            }
            String[] parts = line.split("\\s+");
            if (parts.length >= 5) { // 确保有 Pad Name, X, Y, Area, Width:Height
                validRows++;
            }
        }
        return validRows;
    }

    private int countTableRows(String tableContent) {
        String[] rows = tableContent.split("\n");
        int dataRows = 0;
        for (int i = 2; i < rows.length; i++) { // 跳过表头和分隔行
            String row = rows[i].trim();
            if (!row.isEmpty() && !row.matches("[-\\|]+")) {
                dataRows++;
            }
        }
        return dataRows;
    }

    private String generateTableWithAI(String content, int expectedRows) {
        ChatCompletionRequest chatCompletionRequest = new ChatCompletionRequest();
        chatCompletionRequest.setTemperature(0.2);
        chatCompletionRequest.setMax_tokens(16384);
        chatCompletionRequest.setCategory("default");

        ChatMessage message = new ChatMessage();

        String prompt = "请根据提供的 txt 文件中的焊盘信息，补全 PAD Coordinate 表。\n" +
                "\n" +
                "### 具体要求如下：\n" +
                "1. 对应关系：\n" +
                "   - txt 文件中的 **Pad Opening Name** 列对应表格中的 **PAD NAME**。\n" +
                "   - txt 文件中的 **X** 列对应表格中的 **Coordinate of pad center PRIMARY 0,0 LOWER LEFT (X)**。\n" +
                "   - txt 文件中的 **Y** 列对应表格中的 **Coordinate of pad center PRIMARY 0,0 LOWER LEFT (Y)**。\n" +
                "   - txt 文件中的 **Width:Height** 列中的 Width 值对应表格中的 **PAD OPEN (um)** 列，" +
                "     值需要将 `75.000000 : 75.000000` 转换为 `75.000000*75.000000`（仅使用 Width 值）。\n" +
                "2. PAD# 列：\n" +
                "   - 根据 txt 文件中的行号自动生成，从 1 开始递增。\n" +
                "3. 完整性要求：\n" +
                "   - 必须解析 txt 文件中的每一行有效数据（非空行且列数正确），生成对应表格行，总行数应为 " + expectedRows + "。\n" +
                "   - 跳过文件中的头部信息（以 '*' 开头的行）和注释行（以 '#' 开头的行）。\n" +
                "\n" +
                "### txt 文件格式说明：\n" +
                "- 数据从 'Pad Opening Name' 行后的第一行开始，每行包含以下列，用空格分隔：\n" +
                "  **Pad Opening Name**, **X**, **Y**, **Area**, **Width:Height**。\n" +
                "- 文件可能包含空行、头部信息和注释，均应跳过。\n" +
                "以下是 txt 文件的示例内容：\n" +
                "-------------------------------------\n" +
                "Pad Opening Name      X               Y               Area            Width:Height\n" +
                "GND                   90.550000       1878.900000     5625.000000     75.000000 : 75.000000\n" +
                "VCC                   100.000000      2000.000000     5625.000000     75.000000 : 75.000000\n" +
                "# Comment\n" +
                "ADSE                  90.550000       1781.900000     5625.000000     75.000000 : 75.000000\n" +
                "-------------------------------------\n" +
                "\n" +
                "### 提供的 txt 文件内容：\n" +
                "-------------------------------------\n" +
                content +
                "-------------------------------------\n" +
                "\n" +
                "### 任务目标：\n" +
                "- 逐行解析 txt 文件中的每一行有效数据（从 'Pad Opening Name' 后的数据行开始）。\n" +
                "- 生成完整的 **PAD Coordinate 表格**，确保包含所有 " + expectedRows + " 行有效数据。\n" +
                "\n" +
                "### 输出表格格式：\n" +
                "| PAD# | PAD OPEN (um) | Coordinate of pad center PRIMARY 0,0 LOWER LEFT (X) | Coordinate of pad center PRIMARY 0,0 LOWER LEFT (Y) | PAD NAME |\n" +
                "|---|---|---|---|---|\n" +
                "| 1 | 75*75 | 90.55 | 1878.9 | GND |\n" +
                "| 2 | 75*75 | 100.00 | 2000.0 | VCC |\n" +
                "\n" +
                "请严格按照上述格式，直接生成完整表格数据，不添加任何多余说明或内容。";

        message.setRole("user");
        message.setContent(prompt);
        chatCompletionRequest.setMessages(Lists.newArrayList(message));
        chatCompletionRequest.setStream(false);

        CompletionsService completionsService = new CompletionsService();
        ChatCompletionResult result = completionsService.completions(chatCompletionRequest);

        if (result != null && result.getChoices() != null && !result.getChoices().isEmpty()) {
            return result.getChoices().get(0).getMessage().getContent();
        } else {
            throw new RuntimeException("AI生成失败");
        }
    }

    private String generateFallbackTable(String fileContent) {
        StringBuilder table = new StringBuilder();
        table.append("| PAD# | PAD OPEN (um) | Coordinate of pad center PRIMARY 0,0 LOWER LEFT (X) | Coordinate of pad center PRIMARY 0,0 LOWER LEFT (Y) | PAD NAME |\n");
        table.append("|---|---|---|---|---|\n");

        String[] lines = fileContent.split("\n");
        int padNumber = 1;
        boolean dataSectionStarted = false;
        for (String line : lines) {
            line = line.trim();
            if (line.startsWith("Pad Opening Name")) {
                dataSectionStarted = true;
                continue;
            }
            if (!dataSectionStarted || line.isEmpty() || line.startsWith("*") || line.startsWith("#")) {
                continue;
            }

            String[] parts = line.split("\\s+");
            if (parts.length < 5) {
                System.err.println("跳过无效行: " + line);
                continue;
            }

            String padName = parts[0];
            String x = parts[1];
            String y = parts[2];
            String widthHeight = parts[4];
            String padOpen;
            try {
                String[] whParts = widthHeight.split(":");
                if (whParts.length >= 2) {
                    padOpen = whParts[0] + "*" + whParts[0]; // 使用 Width
                } else {
                    padOpen = "75*75"; // 默认值
                    System.err.println("Width:Height 格式错误，使用默认值: " + line);
                }
            } catch (Exception e) {
                padOpen = "75*75"; // 异常情况下的默认值
                System.err.println("解析 Width:Height 失败，使用默认值: " + line);
            }

            table.append("| ").append(padNumber).append(" | ")
                    .append(padOpen).append(" | ")
                    .append(x).append(" | ")
                    .append(y).append(" | ")
                    .append(padName).append(" |\n");
            padNumber++;
        }
        return table.toString();
    }

    private File generateWordFile(String tableContent) throws IOException {
        File file = File.createTempFile("PadCoordinate", ".docx");
        try (FileOutputStream out = new FileOutputStream(file)) {
            XWPFDocument document = new XWPFDocument();
            XWPFTable table = document.createTable(0, 0);

            String[] rows = tableContent.split("\n");
            String[] headerCells = rows[0].split("\\|");
            XWPFTableRow headerRow = table.createRow();
            for (String cell : headerCells) {
                String trimmedCell = cell.trim();
                if (!trimmedCell.isEmpty()) {
                    headerRow.addNewTableCell().setText(trimmedCell);
                }
            }

            for (int i = 1; i < rows.length; i++) {
                String rowContent = rows[i].trim();
                if (rowContent.isEmpty() || rowContent.matches("[-\\|]+")) {
                    continue;
                }

                String[] cells = rows[i].split("\\|");
                XWPFTableRow row = table.createRow();
                for (String cell : cells) {
                    String trimmedCell = cell.trim();
                    if (!trimmedCell.isEmpty()) {
                        row.addNewTableCell().setText(trimmedCell);
                    }
                }
            }

            document.write(out);
        }
        return file;
    }

    private void sendFile(HttpServletResponse resp, File file) throws IOException {
        resp.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        resp.setHeader("Content-Disposition", "attachment; filename=\"PadCoordinate.docx\"");

        try (FileInputStream in = new FileInputStream(file);
             OutputStream out = resp.getOutputStream()) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
            out.flush();
        } finally {
            file.delete();
        }
    }
}
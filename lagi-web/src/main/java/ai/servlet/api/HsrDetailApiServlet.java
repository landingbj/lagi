package ai.servlet.api;

import ai.common.pojo.Response;
import ai.dto.BlockDesc;
import ai.dto.DxDiagnosis;
import ai.dto.Rectangle;
import ai.llm.service.CompletionsService;
import ai.openai.pojo.ChatCompletionRequest;
import ai.openai.pojo.ChatCompletionResult;
import ai.openai.pojo.ChatMessage;
import ai.servlet.BaseServlet;
import ai.sevice.DxImageService;
import ai.sevice.ImageBlockService;
import ai.utils.MigrateGlobal;
import ai.vector.FileService;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import org.apache.poi.xwpf.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.*;

public class HsrDetailApiServlet extends BaseServlet {
    private static final String UPLOAD_DIR = "/upload";
    private static final int CHUNK_SIZE = 20000;
    private static final Logger logger = LoggerFactory.getLogger(HsrDetailApiServlet.class);
    private final FileService fileService = new FileService();
    private final CompletionsService completionsService = new CompletionsService();
    private final Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        logger.info("收到 POST 请求: {}", req.getRequestURI());
        req.setCharacterEncoding("UTF-8");
        resp.setHeader("Content-Type", "application/json;charset=utf-8");
        String url = req.getRequestURI();
        String method = url.substring(url.lastIndexOf("/") + 1);
        logger.debug("提取的方法: {}", method);

        if (method.equals("generateHsrDetail")) {
            this.generateHsrDetail(req, resp);
        } else {
            logger.warn("不支持的方法: {}", method);
            Response response = Response.builder().status("failed").msg("不支持的方法").build();
            responsePrint(resp, gson.toJson(response));
        }
    }

    private void generateHsrDetail(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        logger.info("开始执行 generateHsrDetail");
        String msg = null;
        List<File> files = new ArrayList<>();

        try {
            logger.debug("尝试获取上传的文件");
            files = getUploadFile(req, MigrateGlobal.DOC_FILE_SIZE_LIMIT, MigrateGlobal.DOC_FILE_SIZE_LIMIT, UPLOAD_DIR);
            logger.info("上传文件数量: {}", files.size());
        } catch (Exception e) {
            msg = "解析文件出现错误";
            logger.error("解析上传文件失败", e);
        }

        if (files.size() == 2) {
            File bdImageFile = files.get(0).getName().endsWith(".pdf") ? files.get(1) : files.get(0);
            File pdfFile = files.get(0).getName().endsWith(".pdf") ? files.get(0) : files.get(1);

            try {
                // Step 1: 分析 BD 图
                logger.info("开始分析 BD 图文件: {}", bdImageFile.getName());
                List<BlockDesc> blockDescList = callImageBlockService(bdImageFile);
                List<DxDiagnosis> dxDiagnoses = callDxImageService(bdImageFile);

                // Step 2: 将 PDF 转换为 Markdown
                logger.info("开始将 PDF 转换为 Markdown，文件: {}", pdfFile.getName());
                Response markdownResponse = fileService.toMarkdown(pdfFile);
                if (markdownResponse == null || !"success".equals(markdownResponse.getStatus())) {
                    String errorMsg = markdownResponse != null ? markdownResponse.getMsg() : "未知错误";
                    logger.error("PDF 转换 Markdown 失败: {}", errorMsg);
                    throw new RuntimeException("PDF 转换为 Markdown 失败: " + errorMsg);
                }
                String markdownContent = markdownResponse.getData().toString();
                logger.info("Markdown 内容长度: {}", markdownContent.length());

                // Step 3: 建立坐标包含关系并准备大模型输入
                logger.info("开始建立坐标包含关系");
                String modelInput = buildModelInput(blockDescList, dxDiagnoses, markdownContent);

                // Step 4: 调用大模型生成 HSR 内容
                logger.info("开始使用 AI 生成 HSR 详情");
                String hsrContent = generateHsrDetailWithAI(modelInput);
                if (hsrContent == null || hsrContent.trim().isEmpty()) {
                    logger.error("HSR 详情生成失败，结果为空");
                    throw new RuntimeException("HSR 详情生成失败，结果为空");
                }
                logger.info("HSR 详情生成完成，长度: {}", hsrContent.length());

                // Step 5: 生成 Word 文件
                logger.info("开始生成 Word 文件");
                File wordFile = generateWordFile(hsrContent);
                logger.info("Word 文件生成成功: {}", wordFile.getAbsolutePath());
                sendFile(resp, wordFile);
                logger.info("Word 文件已发送给客户端");

            } catch (Exception e) {
                logger.error("生成 HSR 详情失败", e);
                resp.getWriter().write("{\"status\": \"failed\", \"msg\": \"生成 HSR 详情失败\"}");
            }
        } else {
            logger.warn("上传文件数量不正确，错误信息: {}", msg);
            Response response = Response.builder().status("failed").msg(msg != null ? msg : "需上传一张图片和一个 PDF 文件").build();
            responsePrint(resp, gson.toJson(response));
        }
    }

    private List<BlockDesc> callImageBlockService(File imageFile) {
        logger.debug("调用 ImageBlockService 分析图片: {}", imageFile.getName());
        ImageBlockService imageBlockService = new ImageBlockService();
        String imagePath = imageFile.getAbsolutePath();
        try {
            return imageBlockService.analyzeBdImage(imagePath);
        } catch (IOException e) {
            throw new RuntimeException("ImageBlockService 分析失败", e);
        }
    }

    private List<DxDiagnosis> callDxImageService(File imageFile) {
        logger.debug("调用 DxImageService 分析图片: {}", imageFile.getName());
        DxImageService dxImageService = new DxImageService();
        String imagePath = imageFile.getAbsolutePath();
        try {
            return dxImageService.analyzeImage(imagePath);
        } catch (Exception e) {
            logger.error("DxImageService 分析失败", e);
            throw new RuntimeException("DxImageService 分析失败");
        }
    }

    private String buildModelInput(List<BlockDesc> blockDescList, List<DxDiagnosis> dxDiagnoses, String markdownContent) {
        logger.debug("开始构建大模型输入数据");
        StringBuilder inputBuilder = new StringBuilder();

        // 添加 BD 图分析结果
        inputBuilder.append("Block Descriptions:\n");
        for (BlockDesc block : blockDescList) {
            inputBuilder.append(String.format("Block ID: %d, Description: %s\n", block.getId(), block.getBlock()));
        }

        inputBuilder.append("\nDx Diagnoses:\n");
        for (DxDiagnosis dx : dxDiagnoses) {
            inputBuilder.append(String.format("ID: %s, Short Description: %s, Detail Description: %s\n",
                    dx.getId(), dx.getShortDesc(), dx.getDetailDesc()));
        }

        // 添加 PDF Markdown 内容
        inputBuilder.append("\nDatasheet Content (Markdown):\n");
        inputBuilder.append(markdownContent);

        // 添加坐标包含关系
        inputBuilder.append("\nBlock-Dx Relationships:\n");
        for (DxDiagnosis dx : dxDiagnoses) {
            Rectangle dxRect = dx.getRectangle();
            for (BlockDesc block : blockDescList) {
                Rectangle blockRect = block.getRectangle();
                if (isRectangleContained(dxRect, blockRect)) {
                    inputBuilder.append(String.format("Dx ID: %s is contained in Block ID: %d\n", dx.getId(), block.getId()));
                }
            }
        }

        logger.info("大模型输入数据构建完成，长度: {}", inputBuilder.length());
        return inputBuilder.toString();
    }

    private boolean isRectangleContained(Rectangle inner, Rectangle outer) {
        return inner.getX0() >= outer.getX0() &&
                inner.getY0() >= outer.getY0() &&
                inner.getX1() <= outer.getX1() &&
                inner.getY1() <= outer.getY1();
    }

    private String generateHsrDetailWithAI(String modelInput) {
        logger.info("开始执行 generateHsrDetailWithAI，输入长度: {}", modelInput.length());
        if (modelInput == null || modelInput.trim().isEmpty()) {
            logger.error("模型输入内容为空，无法处理");
            return null;
        }

        List<String> chunks = splitContentIntoChunks(modelInput);
        logger.info("内容已分割为 {} 个分块", chunks.size());
        List<List<String>> history = new ArrayList<>();

        String initialPrompt = "Analyze the provided content to generate Hardware Safety Requirements (HSR). Do not include emojis, Markdown symbols (e.g., #, *, |), or additional sections like 'Summary of HSR Mapping' or 'Final Notes'. Generate only the HSR entries in plain text with the following format for each DI/DX found:\n" +
                "4.6 Hardware Safety Requirement (HSR<编号>): <需求名称>\n" +
                "Target safety goal ID: <SG ID>\n" +
                "Related technical safety requirement specification (HW allocation): <TSR ID>\n" +
                "Explanation of the HW safety requirement specification\n" +
                "<详细说明>\n" +
                "4.6.1 HW safety requirement specifications\n" +
                "HSR: <HSR ID>\n" +
                "Hardware component name: <组件名称>\n" +
                "Basic event: <基本事件>\n" +
                "Safety requirement: <安全要求>\n" +
                "ASIL: <ASIL>\n" +
                "Fault tolerant time interval: <容错时间>\n" +
                "SSR related: <SSR 相关>\n\n" +
                "Ensure one HSR per DI/DX. Use 'NA' for missing information.";

        StringBuilder finalHsrContent = new StringBuilder();
        logger.debug("初始提示词长度: {}", initialPrompt.length());

        // 处理每个分块
        for (int i = 0; i < chunks.size(); i++) {
            String chunkPrompt = "This is chunk " + (i + 1) + " of " + chunks.size() + ":\n" +
                    "-------------------------------------\n" +
                    chunks.get(i) +
                    "-------------------------------------\n" +
                    (i == chunks.size() - 1 ?
                            "This is the final chunk. Generate the complete Hardware Safety Requirements (HSR) in the specified plain text format, one HSR per DI/DX, without emojis, Markdown symbols, or additional sections like 'Summary of HSR Mapping' or 'Final Notes'." :
                            "Analyze this chunk and provide insights or a partial summary of Hardware Safety Requirements.");
            logger.debug("处理第 {}/{} 块分块，长度: {}", i + 1, chunks.size(), chunks.get(i).length());

            ChatCompletionResult result = callLLm(initialPrompt, history, chunkPrompt);
            if (result == null || result.getChoices() == null || result.getChoices().isEmpty()) {
                logger.error("AI 处理第 {}/{} 块分块失败", i + 1, chunks.size());
                return null;
            }

            String aiResponse = result.getChoices().get(0).getMessage().getContent();
            logger.debug("AI 对第 {}/{} 块的响应，长度: {}, 内容: {}", i + 1, chunks.size(), aiResponse != null ? aiResponse.length() : "null", aiResponse);
            history.add(Lists.newArrayList(chunkPrompt, aiResponse));

            if (i == chunks.size() - 1) {
                if (aiResponse == null) {
                    logger.error("第 {}/{} 块的最终 AI 响应为 null", i + 1, chunks.size());
                    return null;
                } else {
                    finalHsrContent.append(aiResponse);
                    logger.info("最终 HSR 内容已追加，总长度: {}", finalHsrContent.length());
                }
            }
        }

        String hsrResult = finalHsrContent.toString();
        logger.info("generateHsrDetailWithAI 执行完成，结果长度: {}", hsrResult.length());
        return hsrResult;
    }

    private ChatCompletionResult callLLm(String prompt, List<List<String>> history, String userMsg) {
        logger.debug("开始调用 LLM，历史记录大小: {}", history.size());
        ChatCompletionRequest request = new ChatCompletionRequest();
        List<ChatMessage> chatMessages = new ArrayList<>();

        ChatMessage systemMessage = new ChatMessage();
        systemMessage.setContent(prompt);
        systemMessage.setRole("system");
        chatMessages.add(systemMessage);
        logger.debug("添加系统消息，长度: {}", prompt.length());

        for (int i = 0; i < history.size(); i++) {
            ChatMessage userMessage = new ChatMessage();
            userMessage.setRole("user");
            userMessage.setContent(history.get(i).get(0));

            ChatMessage assistantMessage = new ChatMessage();
            assistantMessage.setRole("assistant");
            assistantMessage.setContent(history.get(i).get(1));

            chatMessages.add(userMessage);
            chatMessages.add(assistantMessage);
            logger.debug("添加历史记录第 {} 条，用户消息长度: {}, 助手消息长度: {}", i, history.get(i).get(0).length(), history.get(i).get(1) != null ? history.get(i).get(1).length() : "null");
        }

        ChatMessage userMessage = new ChatMessage();
        userMessage.setRole("user");
        userMessage.setContent(userMsg);
        chatMessages.add(userMessage);
        logger.debug("添加用户消息，长度: {}", userMsg.length());

        request.setMax_tokens(16384);
        request.setTemperature(0.2);
        request.setMessages(chatMessages);

        logger.debug("LLM 请求已准备: {}", gson.toJson(request));
        ChatCompletionResult result = completionsService.completions(request);
        logger.debug("LLM 响应已接收: {}", result != null ? "非空" : "空");
        return result;
    }

    private List<String> splitContentIntoChunks(String content) {
        logger.debug("开始将内容分割为分块，总长度: {}", content.length());
        List<String> chunks = new ArrayList<>();
        int start = 0;
        while (start < content.length()) {
            int end = Math.min(start + CHUNK_SIZE, content.length());
            if (end < content.length()) {
                while (end > start && content.charAt(end) != '\n' && content.charAt(end) != '.') {
                    end--;
                }
                if (end == start) {
                    end = Math.min(start + CHUNK_SIZE, content.length());
                }
            }
            String chunk = content.substring(start, end).trim();
            chunks.add(chunk);
            logger.debug("创建分块，起始: {}, 结束: {}, 长度: {}", start, end, chunk.length());
            start = end + 1;
        }
        logger.info("内容分割为 {} 个分块", chunks.size());
        return chunks;
    }

    private File generateWordFile(String content) throws IOException {
        logger.info("开始生成 Word 文件，内容长度: {}", content != null ? content.length() : "null");
        if (content == null || content.trim().isEmpty()) {
            logger.error("内容为 null 或空，无法生成 Word 文件");
            throw new IllegalArgumentException("生成 Word 文件的内容为 null 或空");
        }

        File file = File.createTempFile("HsrDetail", ".docx");
        logger.debug("创建临时文件: {}", file.getAbsolutePath());

        try (FileOutputStream out = new FileOutputStream(file)) {
            XWPFDocument document = new XWPFDocument();
            String[] lines = content.split("\n");
            logger.debug("内容分割为 {} 行", lines.length);

            for (int i = 0; i < lines.length; i++) {
                String line = lines[i] != null ? lines[i].trim() : "";
                if (line.isEmpty()) {
                    continue;
                }
                logger.debug("处理行内容，行号: {}, 内容: {}", i, line);

                // 处理标题
                if (line.matches("\\d+\\.\\d+\\s+.*")) {
                    XWPFParagraph paragraph = document.createParagraph();
                    XWPFRun run = paragraph.createRun();
                    run.setBold(true);
                    run.setFontSize(14);
                    run.setText(cleanText(line));
                    paragraph.setStyle("Heading1");
                    paragraph.setSpacingAfter(200);
                    logger.debug("格式化为标题，内容: {}", line);
                } else if (line.matches("\\d+\\.\\d+\\.\\d+\\s+.*")) {
                    XWPFParagraph paragraph = document.createParagraph();
                    XWPFRun run = paragraph.createRun();
                    run.setBold(true);
                    run.setFontSize(12);
                    run.setText(cleanText(line));
                    paragraph.setStyle("Heading2");
                    paragraph.setSpacingAfter(200);
                    logger.debug("格式化为子标题，内容: {}", line);
                } else if (line.matches(".*:.*")) {
                    // 处理键值对（如 HSR: <HSR ID>）
                    String[] parts = line.split(":", 2);
                    if (parts.length == 2) {
                        String key = parts[0].trim();
                        String value = parts[1].trim();
                        XWPFTable table = document.createTable(1, 2);
                        XWPFTableRow row = table.getRow(0);
                        row.getCell(0).setText(key);
                        row.getCell(1).setText(value);
                        logger.debug("创建键值对表格，键: {}, 值: {}", key, value);
                    }
                } else {
                    // 处理普通段落
                    XWPFParagraph paragraph = document.createParagraph();
                    XWPFRun run = paragraph.createRun();
                    run.setFontSize(12);
                    run.setText(cleanText(line));
                    paragraph.setSpacingAfter(200);
                    logger.debug("添加段落，内容: {}", line);
                }
            }

            document.write(out);
            logger.info("Word 文件写入成功: {}", file.getAbsolutePath());
        } catch (Exception e) {
            logger.error("生成 Word 文件失败", e);
            throw e;
        }

        return file;
    }

    private String cleanText(String text) {
        return text.replaceAll("[*_`>✅🔧📌#|]+", "").trim();
    }

    private void sendFile(HttpServletResponse resp, File file) throws IOException {
        logger.info("开始发送文件: {}", file.getAbsolutePath());
        resp.setHeader("Content-Type", "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        resp.setHeader("Content-Disposition", "attachment; filename=\"HsrDetail.docx\"");

        try (FileInputStream in = new FileInputStream(file);
             OutputStream out = resp.getOutputStream()) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
            out.flush();
            logger.info("文件发送成功");
        } finally {
            file.delete();
            logger.debug("临时文件删除: {}", file.getAbsolutePath());
        }
    }
}
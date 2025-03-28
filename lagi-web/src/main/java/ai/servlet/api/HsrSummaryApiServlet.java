package ai.servlet.api;

import ai.common.pojo.Response;
import ai.llm.service.CompletionsService;
import ai.openai.pojo.ChatCompletionRequest;
import ai.openai.pojo.ChatCompletionResult;
import ai.openai.pojo.ChatMessage;
import ai.servlet.BaseServlet;
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
import java.util.ArrayList;
import java.util.List;

public class HsrSummaryApiServlet extends BaseServlet {
    private static final String UPLOAD_DIR = "/upload";
    private static final int CHUNK_SIZE = 20000; // 增大到20000字符，减少分块数量
    private static final Logger logger = LoggerFactory.getLogger(HsrSummaryApiServlet.class);
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

        if (method.equals("generateHsrSummary")) {
            this.generateHsrSummary(req, resp);
        } else {
            logger.warn("不支持的方法: {}", method);
        }
    }

    private void generateHsrSummary(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        logger.info("开始执行 generateHsrSummary");
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

        if (!files.isEmpty()) {
            try {
                // Step 1: 将 PDF 转换为 Markdown
                logger.info("开始将 PDF 转换为 Markdown，文件: {}", files.get(0).getName());
                Response markdownResponse = fileService.toMarkdown(files.get(0));
                if (markdownResponse == null || !"success".equals(markdownResponse.getStatus())) {
                    String errorMsg = markdownResponse != null ? markdownResponse.getMsg() : "未知错误";
                    logger.error("PDF 转换 Markdown 失败: {}", errorMsg);
                    throw new RuntimeException("PDF 转换为 Markdown 失败: " + errorMsg);
                }
                String markdownContent = markdownResponse.getData().toString();
                logger.info("Markdown 内容长度: {}", markdownContent.length());

                // Step 2: 使用多轮对话生成 HSR Summary
                logger.info("开始使用 AI 生成 HSR Summary");
                String hsrSummary = generateHsrSummaryWithAI(markdownContent);
                if (hsrSummary == null || hsrSummary.trim().isEmpty()) {
                    logger.error("HSR Summary 生成失败，结果为空");
                    throw new RuntimeException("HSR Summary 生成失败，结果为空");
                }
                logger.info("HSR Summary 生成完成，长度: {}", hsrSummary.length());

                // Step 3: 生成并发送 Word 文件
                logger.info("开始生成 Word 文件");
                File wordFile = generateWordFile(hsrSummary);
                logger.info("Word 文件生成成功: {}", wordFile.getAbsolutePath());
                sendFile(resp, wordFile);
                logger.info("Word 文件已发送给客户端");

            } catch (Exception e) {
                logger.error("生成 HSR Summary 失败", e);
                resp.getWriter().write("{\"status\": \"failed\", \"msg\": \"生成 HSR Summary 失败\"}");
            }
        } else {
            logger.warn("没有上传文件，错误信息: {}", msg);
            Response response = Response.builder().status("failed").msg(msg != null ? msg : "未上传文件").build();
            responsePrint(resp, gson.toJson(response));
        }
    }

    private String generateHsrSummaryWithAI(String markdownContent) {
        logger.info("开始执行 generateHsrSummaryWithAI，Markdown 长度: {}", markdownContent.length());
        if (markdownContent == null || markdownContent.trim().isEmpty()) {
            logger.error("Markdown 内容为空，无法处理");
            return null;
        }

        List<String> chunks = splitContentIntoChunks(markdownContent);
        logger.info("内容已分割为 {} 个分块", chunks.size());
        List<List<String>> history = new ArrayList<>();

        String initialPrompt = "You are a professional technical writing assistant. Your task is to generate a new **Hardware Safety Specification Requirement (HSR) Summary** based on the provided input file, which is a hardware safety specification document aligned with the ISO 26262 standard. Since the file is lengthy, I will provide the content in chunks. Analyze each chunk incrementally, infer and expand upon the content based on your understanding, and produce a structured, professional, and comprehensive summary in English. Below are the detailed requirements:\n\n" +
                "### Task Requirements:\n" +
                "1. **Understand the Content and Structure of the Input Document**\n" +
                "   - The input file is based on the ISO 26262 standard and contains detailed hardware safety requirements (HSR).\n" +
                "   - Extract and comprehend key elements such as:\n" +
                "     - Overall structure and format of the document\n" +
                "     - Safety requirements related to functional block diagrams, operational requirements, and timing requirements\n" +
                "     - Fault detection, reaction time, single-point fault, and latent fault metrics\n" +
                "     - Hardware architecture and associated safety functions\n" +
                "     - Quality requirements and compliance with industry standards (e.g., ISO 26262, AEC-Q100)\n" +
                "   - Use your industry knowledge to infer and expand upon the content.\n" +
                "2. **Generate a New Summary Document with the Following Structure and Examples:**\n" +
                "   - **1.0 Purpose**\n" +
                "     - Provide a high-level overview of the document’s purpose, explaining the project’s hardware safety requirements.\n" +
                "     - Example: \n" +
                "       > This document defines the XXXX project hardware safety requirement specification. Safety requirements related to functional block diagrams, function operations, and timing requirements would be defined.\n" +
                "     - Replace 'XXXX' with the inferred project name or context from the input and expand accordingly.\n" +
                "   - **2.0 Scope**\n" +
                "     - Summarize the document’s scope, including applicable standards, compatibility requirements, and target configurations.\n" +
                "     - Example:\n" +
                "       > Compatible with JEDEC79-3 DDR3 SDRAM standard. Memory capacity is 4Gb, supports both x16/x8 IO configurations, and both 1.5V/1.35V operation. DDR3 SDRAM is the next generation of DDR2 SDRAM, which is targeted to have higher data bandwidth and lower power consumption compared to DDR2. Some new features such as read/write calibration are added in DDR3 SDRAM.\n" +
                "     - Adapt based on the input content and infer potential use cases.\n" +
                "   - **3.0 Terms and Definitions**\n" +
                "     - Provide a list of key terms and definitions related to hardware safety, using the following specific terms unless the input explicitly contradicts them:\n" +
                "       > DDR: Double Data Rate\n" +
                "       > ECC: Error Correction Code\n" +
                "       > SER: Soft Error Rate\n" +
                "       > SDRAM: Synchronous Dynamic Random-access Memory\n" +
                "       > ASIL: Automotive Safety Integrity Level\n" +
                "       > PMHF: Probabilistic Metric for random Hardware Failures\n" +
                "       > SPFM: Single Point Faults Metric\n" +
                "       > LFM: Latent Faults Metric\n" +
                "     - Format as a list (not a table) with each term followed by its definition on a new line.\n" +
                "   - **4.0 Hardware Safety Specification Requirement**\n" +
                "     - Summarize key hardware safety requirements based on Technical Safety Requirements (TSR), with expansions.\n" +
                "     - Example:\n" +
                "       > XXXX is targeted to meet safety requirements of ASIL B. According to the Technical Safety Requirement defined in “TSC-TSR assumption”, two new functions need to be supported for safety improvement.\n" +
                "     - Include a table summarizing TSR and HSR relationships:\n" +
                "       > Table 1: Hardware safety requirement from TSR\n" +
                "       > TSR | TSR Description | HSR | HSR Description\n" +
                "       > TSR1 | Faults in the DRAM chip which have the potential to send incorrect data to the CPU/MPU shall be detected and corrected within XX ms. | HSR1 | Need to support ECC function\n" +
                "       > TSR2 | If the incorrect data cannot be corrected, the DRAM chip shall inform the CPU/MPU within XX ms. | HSR2 | Need to support an error alert function\n" +
                "     - Replace 'XXXX' with the inferred project name and adjust timing/values based on the input.\n" +
                "   - **4.1 Hardware Safety Requirement (HSR1)**\n" +
                "     - Detail the safety requirement specifications and related concepts.\n" +
                "     - Example:\n" +
                "       > Target safety goal ID: [Infer from input]\n" +
                "       > Related technical safety requirement specification (HW allocation): [Infer from input]\n" +
                "       > Explanation of the HW safety requirement specification\n" +
                "       > In order to meet TSR, the ECC function is required in this 4Gb DDR3. It implements an On-Chip ECC circuit per half-byte (SEC-DEC code): Single Error Correction, Double Error Detector and corrects all single-bit errors, and detects double-bit errors, including those introduced by SER events such as rays, alpha particles, to improve reliability.\n" +
                "     - **4.1.1 HW Safety Requirement Specifications**\n" +
                "       - Table format: ID | Hardware component name | Basic event | Safety requirement | ASIL | Fault tolerant time interval | SSR related\n" +
                "     - **4.1.2 HW Safety Requirement Specification: Latent Fault Avoidance**\n" +
                "       - Table format: ID | Hardware component name | Basic event | Safety requirement | ASIL | Multiple-point fault detection interval | SSR related\n" +
                "     - **4.1.3 Hardware Architecture Diagram**\n" +
                "       - Summarize the hardware architecture reflecting HSR-aligned safety functions (text description only, no diagram generation).\n" +
                "   - **4.2 Hardware Safety Requirement (HSR2)**\n" +
                "     - Follow the same structure as 4.1 for HSR2.\n" +
                "   - **5.0 Hardware Metric Target Value**\n" +
                "     - Summarize target values for SPFM, LFM, and include placeholders for results.\n" +
                "     - Example:\n" +
                "       > XXXX targets ASIL B in the table below. SPFM ≥90%, LFM ≥60%.\n" +
                "       > Result Value: SPFM: [Fill the blank after FMEDA is done] LFM: [Fill the blank after FMEDA is done]\n" +
                "   - **6.0 PMHF Target Value**\n" +
                "     - Summarize PMHF target values separately.\n" +
                "     - Example:\n" +
                "       > XXXX targets ASIL B as the table below. PMHF <10⁻⁷ h⁻¹\n" +
                "       > Result Value: PMHF: [Fill the blank after FMEDA is done]\n" +
                "   - **7.0 Quality Requirement**\n" +
                "     - Summarize quality requirements aligned with AEC-Q100 and other standards.\n" +
                "     - Example:\n" +
                "       > This project has to meet AEC-Q100 requirements.\n" +
                "   - **8.0 Reference Document**\n" +
                "     - List key reference documents and standards.\n" +
                "     - Example:\n" +
                "       > (1) ISO 26262-5:2018 Standard\n" +
                "       > (2) AEC-Q100 Standard\n" +
                "       > (3) [Infer additional references from input]\n" +
                "3. **Formatting Requirements**\n" +
                "   - Use the exact numbering (e.g., '1.0', '2.0') and section titles as specified.\n" +
                "   - Use plain text for terms and definitions (not tables unless specified).\n" +
                "   - Use tables where explicitly requested (e.g., 4.0, 4.1.1, 4.1.2).\n" +
                "   - Ensure consistent technical terms and professional English.\n" +
                "4. **Summarization and Expansion Requirements**\n" +
                "   - Do not copy the input file verbatim; extract key insights and summarize them.\n" +
                "   - Expand and adapt content based on the input and industry standards, ensuring a deep understanding of hardware safety requirements.\n" +
                "### Processing Workflow\n" +
                "   - Analyze each chunk and provide insights or partial summaries.\n" +
                "   - After processing the final chunk, generate the complete HSR Summary in English following the exact structure and examples provided above.\n" +
                "Please start analyzing the first chunk.";

        StringBuilder finalSummary = new StringBuilder();
        logger.debug("初始提示词长度: {}", initialPrompt.length());

        // 处理每个分块
        for (int i = 0; i < chunks.size(); i++) {
            String chunkPrompt = "This is chunk " + (i + 1) + " of " + chunks.size() + ":\n" +
                    "-------------------------------------\n" +
                    chunks.get(i) +
                    "-------------------------------------\n" +
                    (i == chunks.size() - 1 ? "This is the final chunk. Please provide the complete HSR Summary now." : "Please analyze this chunk and provide insights or a partial summary.");
            logger.debug("处理第 {}/{} 块分块，长度: {}", i + 1, chunks.size(), chunks.get(i).length());

            ChatCompletionResult result = callLLm(initialPrompt, history, chunkPrompt);
            if (result == null || result.getChoices() == null || result.getChoices().isEmpty()) {
                logger.error("AI 处理第 {}/{} 块分块失败", i + 1, chunks.size());
                return null; // 提前返回，避免后续空指针
            }

            String aiResponse = result.getChoices().get(0).getMessage().getContent();
            logger.debug("AI 对第 {}/{} 块的响应，长度: {}, 内容: {}", i + 1, chunks.size(), aiResponse != null ? aiResponse.length() : "null", aiResponse);
            history.add(Lists.newArrayList(chunkPrompt, aiResponse));

            if (i == chunks.size() - 1) {
                if (aiResponse == null) {
                    logger.error("第 {}/{} 块的最终 AI 响应为 null", i + 1, chunks.size());
                    return null; // 提前返回
                } else {
                    finalSummary.append(aiResponse);
                    logger.info("最终摘要已追加，总长度: {}", finalSummary.length());
                }
            }
        }

        String summaryResult = finalSummary.toString();
        logger.info("generateHsrSummaryWithAI 执行完成，结果长度: {}", summaryResult.length());
        return summaryResult;
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

        File file = File.createTempFile("HsrSummary", ".docx");
        logger.debug("创建临时文件: {}", file.getAbsolutePath());

        try (FileOutputStream out = new FileOutputStream(file)) {
            XWPFDocument document = new XWPFDocument();
            String[] lines = content.split("\n");
            logger.debug("内容分割为 {} 行", lines.length);

            XWPFTable currentTable = null; // 用于跟踪当前表格
            List<String> tableRows = new ArrayList<>(); // 暂存表格行

            for (int i = 0; i < lines.length; i++) {
                String line = lines[i] != null ? lines[i].trim() : "";
                if (line.isEmpty()) {
                    continue; // 跳过空行
                }
                logger.debug("处理行内容，行号: {}, 内容: {}", i, line);

                // 处理标题（支持 # 和数字编号）
                // 处理标题（支持 # 和数字编号）
                if (line.matches("\\d+\\.\\d+(\\.\\d+)?\\s+.*") || line.startsWith("#")) {
                    if (currentTable != null) {
                        flushTable(document, currentTable, tableRows); // 结束上一个表格
                        currentTable = null;
                        tableRows.clear();
                    }

                    XWPFParagraph paragraph = document.createParagraph();
                    XWPFRun run = paragraph.createRun();
                    int headingLevel = calculateHeadingLevel(line);
                    String cleanText = cleanHeading(line); // 清理标题，确保移除 ** 等标记

                    run.setBold(true);
                    run.setFontSize(12 + (4 - headingLevel) * 2); // 动态调整字体大小
                    run.setText(cleanText);
                    paragraph.setStyle("Heading" + headingLevel); // 设置 Word 标题样式
                    logger.debug("格式化为标题，层级: {}, 内容: {}", headingLevel, cleanText);
                } else if (line.startsWith("|") && line.endsWith("|")) {
                    if (currentTable == null && tableRows.isEmpty()) {
                        tableRows.add(line);
                    } else if (!line.contains("---")) { // 忽略分隔符行
                        tableRows.add(line);
                    }

                    // 检测表格结束
                } else if (!tableRows.isEmpty()) {
                    if (currentTable == null) {
                        currentTable = createTableFromRows(document, tableRows);
                    }
                    flushTable(document, currentTable, tableRows);
                    currentTable = null;
                    tableRows.clear();
                    addParagraph(document, cleanMarkdownText(line)); // 处理当前非表格行

                    // 处理普通段落
                } else {
                    addParagraph(document, cleanMarkdownText(line));
                }
            }

            // 处理未结束的表格
            if (!tableRows.isEmpty() && currentTable != null) {
                flushTable(document, currentTable, tableRows);
            }

            document.write(out);
            logger.info("Word 文件写入成功: {}", file.getAbsolutePath());
        } catch (Exception e) {
            logger.error("生成 Word 文件失败", e);
            throw e;
        }

        return file;
    }

    // 计算标题层级
    private int calculateHeadingLevel(String line) {
        if (line.matches("\\d+\\.\\d+(\\.\\d+)?\\s+.*")) {
            return line.split("\\.").length - 1; // 1.0 -> 1, 1.1.1 -> 3
        }
        int level = 0;
        while (line.startsWith("#")) {
            level++;
            line = line.substring(1).trim();
        }
        return Math.min(level, 4); // 限制最大层级为 4
    }

    // 清理标题文本
    private String cleanHeading(String line) {
        // 移除 Markdown 标题标记 (#) 和加粗标记 (**)
        String cleaned = line.replaceAll("^#+\\s+", "")  // 移除前导的 # 和空格
                .replaceAll("[*_`]+", "")   // 移除加粗 (* 或 **)、斜体 (_) 和代码标记 (`)
                .trim();                   // 移除首尾空格
        return cleaned;
    }

    // 清理普通 Markdown 文本
    private String cleanMarkdownText(String text) {
        return text.replaceAll("[*_`>]+", "").trim(); // 移除加粗、斜体、引用等标记
    }

    // 创建表格
    private XWPFTable createTableFromRows(XWPFDocument document, List<String> tableRows) {
        int columnCount = tableRows.get(0).split("\\|").length - 2;
        XWPFTable table = document.createTable(tableRows.size(), columnCount);
        return table;
    }

    // 填充表格内容
    private void flushTable(XWPFDocument document, XWPFTable table, List<String> tableRows) {
        for (int rowIdx = 0; rowIdx < tableRows.size(); rowIdx++) {
            String[] cells = tableRows.get(rowIdx).split("\\|");
            XWPFTableRow row = table.getRow(rowIdx);
            for (int colIdx = 1; colIdx < cells.length - 1; colIdx++) {
                if (row.getCell(colIdx - 1) == null) {
                    row.addNewTableCell();
                }
                row.getCell(colIdx - 1).setText(cells[colIdx].trim());
                logger.debug("填充表格单元格，行: {}, 列: {}, 内容: {}", rowIdx, colIdx - 1, cells[colIdx].trim());
            }
        }
    }

    // 添加普通段落
    private void addParagraph(XWPFDocument document, String text) {
        XWPFParagraph paragraph = document.createParagraph();
        XWPFRun run = paragraph.createRun();
        run.setText(text);
        logger.debug("添加段落，内容: {}", text);
    }


    private void sendFile(HttpServletResponse resp, File file) throws IOException {
        logger.info("开始发送文件: {}", file.getAbsolutePath());
        resp.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        resp.setHeader("Content-Disposition", "attachment; filename=\"HsrSummary.docx\"");

        try (FileInputStream in = new FileInputStream(file);
             OutputStream out = resp.getOutputStream()) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
            out.flush();
            logger.info("文件发送成功");
        } finally {
            file.delete();
            logger.debug("临时文件已删除: {}", file.getAbsolutePath());
        }
    }
}
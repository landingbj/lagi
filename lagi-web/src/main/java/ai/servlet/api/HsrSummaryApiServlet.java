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

        String initialPrompt = "You are a professional technical writing assistant. Your task is to generate a new **Hardware Safety Specification Requirement (HSR) Summary** based on the provided input file. The input file is a hardware safety specification document based on the ISO 26262 standard. Since the file is lengthy, I will provide the content in chunks. Please analyze each chunk incrementally, infer and expand upon the content based on your understanding, and finally produce a structured, professional, and comprehensive summary in English. Below are the detailed requirements:\n\n" +
                "### Task Requirements:\n" +
                "1. **Understand the Content and Structure of the Input Document**\n" +
                "   - The input file is based on the ISO 26262 standard and contains detailed hardware safety requirements (HSR).\n" +
                "   - Extract and comprehend the following key elements:\n" +
                "     - Overall structure and format of the document\n" +
                "     - Safety requirements related to functional block diagrams, operational requirements, and timing requirements\n" +
                "     - Fault detection, reaction time, single-point fault, and latent fault metrics\n" +
                "     - Hardware architecture and associated safety functions\n" +
                "     - Quality requirements and compliance with industry standards (e.g., ISO 26262, AEC-Q100)\n" +
                "   - Use your industry knowledge to infer and expand upon the content.\n" +
                "2. **Generate a New Summary Document with the Following Structure:**\n" +
                "   - **1.0 Purpose**\n" +
                "     - Provide a high-level overview of the document’s purpose, explaining the project’s hardware safety requirements.\n" +
                "     - Example: \n" +
                "       > This document defines the XXXX project hardware safety requirement specification, covering safety requirements related to functional block diagrams, function operations, and timing requirements.\n" +
                "     - Infer the project context from the input and expand the description accordingly.\n" +
                "   - **2.0 Scope**\n" +
                "     - Summarize the document’s scope, including applicable standards, compatibility requirements, and target configurations.\n" +
                "     - Example:\n" +
                "       > Compatible with JEDEC79-3 DDR3 SDRAM standard. Memory capacity is 4Gb, supporting both x16/x8 IO configurations and 1.5V/1.35V operation. DDR3 SDRAM is the next generation of DDR2 SDRAM, targeting higher data bandwidth and lower power consumption.\n" +
                "     - Infer potential use cases and add relevant details based on the input.\n" +
                "   - **3.0 Terms and Definitions**\n" +
                "     - Provide a list of key terms and definitions related to hardware safety, derived from the input and expanded with common industry terms.\n" +
                "     - Example:\n" +
                "       | Acronym | Definition |\n" +
                "       |---------|------------|\n" +
                "       | DDR     | Double Data Rate |\n" +
                "       | ASIL    | Automotive Safety Integrity Level |\n" +
                "   - **4.0 Hardware Safety Specification Requirement**\n" +
                "     - Summarize key hardware safety requirements based on Technical Safety Requirements (TSR), with expansions.\n" +
                "     - Example:\n" +
                "       > XXXX is targeted to meet ASIL B safety requirements. Based on the Technical Safety Requirements, two new functions are required to enhance safety.\n" +
                "     - **4.1 Hardware Safety Requirement (HSR1)**\n" +
                "       - **4.1.1 HW Safety Requirement Specification**: Detail the safety requirement specifications.\n" +
                "       - **4.1.2 HW Safety Requirement Specification: Latent Fault Avoidance**: Explain how the hardware design avoids latent faults.\n" +
                "       - **4.1.3 Hardware Architecture Diagram**: Summarize the hardware architecture reflecting HSR-aligned safety functions.\n" +
                "   - **5.0 Hardware Metric Target Value**\n" +
                "     - Summarize target values for Single Point Fault Metric (SPFM), Latent Fault Metric (LFM), and Probabilistic Metric for random Hardware Failures (PMHF).\n" +
                "     - Example:\n" +
                "       | ASIL Level | SPFM | LFM | PMHF |\n" +
                "       |------------|------|-----|------|\n" +
                "       | ASIL B     | ≥90% | ≥60% | <10⁻⁷ h⁻¹ |\n" +
                "   - **6.0 PMHF Target Value**\n" +
                "     - Summarize the target values for PMHF separately.\n" +
                "   - **7.0 Quality Requirement**\n" +
                "     - Summarize quality requirements aligned with AEC-Q100 and other standards.\n" +
                "   - **8.0 Reference Document**\n" +
                "     - Summarize key reference documents and standards (e.g., ISO 26262-5:2018, AEC-Q100).\n" +
                "3. **Formatting Requirements**\n" +
                "   - Follow the numbering and sectioning format of the input document.\n" +
                "   - Ensure consistent use of technical terms and professional English throughout.\n" +
                "4. **Summarization and Expansion Requirements**\n" +
                "   - Do not directly copy the input file; instead, extract key insights and summarize them.\n" +
                "   - Expand and adapt the content based on the input structure and industry standards, ensuring a deep understanding of hardware safety requirements.\n" +
                "### Processing Workflow\n" +
                "   - Analyze each chunk and provide insights or partial summaries.\n" +
                "   - After processing the final chunk, generate the complete HSR Summary in English.\n" +
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

            for (int i = 0; i < lines.length; i++) {
                String line = lines[i];
                if (line == null || line.trim().isEmpty()) {
                    logger.debug("跳过 null 或空行，行号: {}", i);
                    continue; // 不保留空行
                }

                logger.debug("处理行内容，行号: {}, 内容: {}", i, line);
                try {
                    // 处理标题行（格式: 1.0 或 2.1）
                    if (line.matches("\\d+\\.\\d+\\s+.*")) {
                        XWPFParagraph paragraph = document.createParagraph();
                        XWPFRun run = paragraph.createRun();
                        run.setBold(true);
                        run.setFontSize(14);
                        run.setText(line);
                        logger.debug("格式化为章节标题，行号: {}, 内容: {}", i, line);

                        // 处理表格行（格式: | xxx | xxx |）
                    } else if (line.startsWith("|") && line.endsWith("|")) {
                        String[] cells = line.split("\\|");
                        int columnCount = cells.length - 2; // 减去首尾空元素
                        if (columnCount <= 0) {
                            logger.debug("无效表格行，跳过，行号: {}", i);
                            continue;
                        }

                        try {
                            // 创建表格并初始化列数（最小为 1 列）
                            XWPFTable table = document.createTable(1, Math.max(columnCount, 1));
                            XWPFTableRow row = table.getRow(0);
                            if (row == null) {
                                logger.error("表格行创建失败，行号: {}", i);
                                continue;
                            }

                            boolean isHeader = line.contains("---");

                            // 填充第一行
                            for (int j = 1; j < cells.length - 1; j++) {
                                String trimmedCell = cells[j].trim();
                                if (!trimmedCell.isEmpty()) {
                                    if (row.getCell(j - 1) == null) {
                                        row.addNewTableCell(); // 动态增加单元格，防止空指针
                                    }
                                    row.getCell(j - 1).setText(trimmedCell);
                                    logger.debug("添加表格单元格，行号: {}, 单元格: {}", i, trimmedCell);
                                }
                            }

                            // 如果不是表头，删除第一行并重新创建
                            if (!isHeader && columnCount > 0) {
                                table.removeRow(0);
                                XWPFTableRow newRow = table.createRow();
                                for (int j = 1; j < cells.length - 1; j++) {
                                    String trimmedCell = cells[j].trim();
                                    if (!trimmedCell.isEmpty()) {
                                        if (newRow.getCell(j - 1) == null) {
                                            newRow.addNewTableCell(); // 动态增加单元格
                                        }
                                        newRow.getCell(j - 1).setText(trimmedCell);
                                        logger.debug("添加表格数据单元格，行号: {}, 单元格: {}", i, trimmedCell);
                                    }
                                }
                            }
                        } catch (Exception e) {
                            logger.error("处理表格行失败，行号: {}, 内容: {}", i, line, e);
                            throw e;
                        }

                        // 处理普通段落
                    } else {
                        XWPFParagraph paragraph = document.createParagraph();
                        XWPFRun run = paragraph.createRun();
                        run.setText(line);
                        logger.debug("设置段落文本，行号: {}, 内容: {}", i, line);
                    }
                } catch (Exception e) {
                    logger.error("处理行失败，行号: {}, 内容: {}", i, line, e);
                    throw e;
                }
            }

            // 将生成的内容写入文件
            document.write(out);
            logger.info("Word 文件写入成功: {}", file.getAbsolutePath());
        } catch (Exception e) {
            logger.error("生成 Word 文件失败", e);
            throw e;
        }

        return file;
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
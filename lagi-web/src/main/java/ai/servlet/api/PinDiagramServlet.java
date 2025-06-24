package ai.servlet.api;

import ai.common.pojo.Response;
import ai.llm.service.CompletionsService;
import ai.openai.pojo.ChatCompletionRequest;
import ai.openai.pojo.ChatCompletionResult;
import ai.openai.pojo.ChatMessage;
import ai.servlet.BaseServlet;
import ai.vector.FileService;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.poi.xslf.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

public class PinDiagramServlet extends BaseServlet {
    private static final String UPLOAD_DIR = "upload";
    private static final Logger logger = LoggerFactory.getLogger(PinDiagramServlet.class);
    private final Gson gson = new Gson();
    private final FileService fileService = new FileService();
    private final CompletionsService completionsService = new CompletionsService();
    private static final int CHUNK_SIZE = 20000;
    private static final String[] KEYWORDS = {
            "PIN CONFIGURATION", // 映射到 Pinout & Pin Function
            "PIN DESCRIPTION",   // 映射到 Pinout & Pin Function
            "Typical Application Circuit",
            "ELECTRICAL CHARACTERISTICS"
    };
    private static final String[] SLIDE_TITLES = {
            "Product Overview",
            "Architecture (Block Diagram)",
            "Pinout & Pin Function",
            "Typical Application Circuit",
            "EC SPEC",
            "Package & Pad"
    };

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        logger.info("Received POST request: {}", req.getRequestURI());
        req.setCharacterEncoding("UTF-8");
        resp.setHeader("Content-Type", "application/json;charset=utf-8");

        if (!ServletFileUpload.isMultipartContent(req)) {
            logger.warn("Request is not multipart/form-data");
            Response response = Response.builder().status("failed").msg("Invalid request format").build();
            responsePrint(resp, gson.toJson(response));
            return;
        }

        Path uploadPath = Paths.get(getServletContext().getRealPath("/") + File.separator + UPLOAD_DIR);
        try {
            Files.createDirectories(uploadPath);
            logger.info("Upload directory ensured: {}", uploadPath.toAbsolutePath());
        } catch (IOException e) {
            logger.error("Failed to create upload directory: {}", uploadPath, e);
            Response response = Response.builder().status("failed").msg("Failed to create upload directory").build();
            responsePrint(resp, gson.toJson(response));
            return;
        }

        DiskFileItemFactory factory = new DiskFileItemFactory();
        factory.setRepository(uploadPath.toFile());
        ServletFileUpload upload = new ServletFileUpload(factory);
        upload.setSizeMax(10 * 1024 * 1024);

        String keyword = null;
        String pptType = null;
        File pdfFile = null;
        File architectureImage = null;
        File packagePadImage = null;

        try {
            List<FileItem> items = upload.parseRequest(req);
            logger.info("Number of form items: {}", items.size());

            for (FileItem item : items) {
                if (item.isFormField()) {
                    String fieldName = item.getFieldName();
                    String fieldValue = item.getString("UTF-8");
                    if ("keyword".equals(fieldName)) {
                        keyword = fieldValue;
                    } else if ("pptType".equals(fieldName)) {
                        pptType = fieldValue; // full 或 partial
                    }
                    logger.debug("Form field: {} = {}", fieldName, fieldValue);
                } else {
                    String fileName = item.getName();
                    if (fileName != null && !fileName.isEmpty()) {
                        fileName = fileName.replaceAll("[^a-zA-Z0-9.-]", "_");
                        fileName = System.currentTimeMillis() + "_" + fileName;
                        String fieldName = item.getFieldName();
                        if ("pdfFile".equals(fieldName)) {
                            pdfFile = new File(uploadPath.toFile(), fileName);
                            item.write(pdfFile);
                            logger.info("Uploaded PDF file saved: {} (size: {} bytes)", pdfFile.getAbsolutePath(), pdfFile.length());
                        } else if ("architectureImage".equals(fieldName)) {
                            architectureImage = new File(uploadPath.toFile(), fileName);
                            item.write(architectureImage);
                            logger.info("Uploaded Architecture image saved: {} (size: {} bytes)", architectureImage.getAbsolutePath(), architectureImage.length());
                        } else if ("packagePadImage".equals(fieldName)) {
                            packagePadImage = new File(uploadPath.toFile(), fileName);
                            item.write(packagePadImage);
                            logger.info("Uploaded Package & Pad image saved: {} (size: {} bytes)", packagePadImage.getAbsolutePath(), packagePadImage.length());
                        }
                    }
                }
            }

            if (pdfFile == null || !pdfFile.exists()) {
                logger.warn("No PDF file uploaded or file not saved");
                Response response = Response.builder().status("failed").msg("No PDF file uploaded").build();
                responsePrint(resp, gson.toJson(response));
                return;
            }

            if (keyword != null && !keyword.trim().isEmpty()) {
                queryImage(pdfFile, keyword, resp);
            } else {
                generatePPT(pdfFile, architectureImage, packagePadImage, pptType, resp);
            }
        } catch (Exception e) {
            logger.error("Request processing failed", e);
            Response response = Response.builder().status("failed").msg("Request processing failed: " + e.getMessage()).build();
            responsePrint(resp, gson.toJson(response));
        } finally {
            // 保留临时文件用于调试
            if (pdfFile != null && pdfFile.exists()) {
                logger.info("Temporary PDF file retained for debugging: {}", pdfFile.getAbsolutePath());
            }
            if (architectureImage != null && architectureImage.exists()) {
                logger.info("Temporary Architecture image retained for debugging: {}", architectureImage.getAbsolutePath());
            }
            if (packagePadImage != null && packagePadImage.exists()) {
                logger.info("Temporary Package & Pad image retained for debugging: {}", packagePadImage.getAbsolutePath());
            }
        }
    }

    private void generatePPT(File pdfFile, File architectureImage, File packagePadImage, String pptType, HttpServletResponse resp) {
        logger.info("Generating PPT for file: {}, pptType: {}", pdfFile.getName(), pptType);
        List<String> imagePaths = new ArrayList<>();
        Path uploadPath = Paths.get(getServletContext().getRealPath("/") + File.separator + UPLOAD_DIR);

        try {
            // Step 1: 生成 Product Overview
            String productOverview = generateProductOverview(pdfFile);
            if (productOverview == null || productOverview.trim().isEmpty()) {
                logger.warn("Failed to generate Product Overview");
            }

            // Step 2: 收集截图
            for (String keyword : KEYWORDS) {
                String imagePath = callPythonService(pdfFile, keyword);
                if (imagePath != null && new File(imagePath).exists() && new File(imagePath).length() > 0) {
                    String debugImageName = "debug_" + keyword.replaceAll("[^a-zA-Z0-9]", "_") + ".png";
                    Files.copy(Paths.get(imagePath), uploadPath.resolve(debugImageName), StandardCopyOption.REPLACE_EXISTING);
                    logger.info("Debug image saved: {}", uploadPath.resolve(debugImageName));
                    imagePaths.add(imagePath);
                    logger.info("Image generated for keyword '{}': {} (size: {} bytes)", keyword, imagePath, new File(imagePath).length());
                } else {
                    logger.warn("No valid image for keyword '{}'", keyword);
                }
            }

            // Step 3: 创建 PPT
            XMLSlideShow ppt = new XMLSlideShow();
            XSLFSlideMaster master = ppt.getSlideMasters().get(0);
            XSLFSlideLayout layout = master.getLayout(SlideLayout.TITLE_AND_CONTENT);

            // 添加 Product Overview 幻灯片
            if (productOverview != null && !productOverview.trim().isEmpty()) {
                XSLFSlide slide = ppt.createSlide(layout);
                XSLFTextShape title = slide.getPlaceholder(0);
                title.clearText();
                XSLFTextParagraph titleP = title.addNewTextParagraph();
                XSLFTextRun titleR = titleP.addNewTextRun();
                titleR.setText(SLIDE_TITLES[0]);
                titleR.setFontSize(28.0);
                titleR.setFontColor(Color.BLACK);

                XSLFTextShape content = slide.getPlaceholder(1);
                content.clearText();
                String[] lines = productOverview.split("\n");
                for (String line : lines) {
                    XSLFTextParagraph p = content.addNewTextParagraph();
                    XSLFTextRun r = p.addNewTextRun();
                    r.setText(line);
                    r.setFontSize(20.0);
                    r.setFontColor(Color.BLACK);
                }
                logger.debug("Added Product Overview slide");
            }

            // 添加 Architecture (Block Diagram) 幻灯片
            if (architectureImage != null && architectureImage.exists() && architectureImage.length() > 0) {
                XSLFSlide slide = ppt.createSlide(layout);
                XSLFTextShape title = slide.getPlaceholder(0);
                title.clearText();
                XSLFTextParagraph titleP = title.addNewTextParagraph();
                XSLFTextRun titleR = titleP.addNewTextRun();
                titleR.setText(SLIDE_TITLES[1]);
                titleR.setFontSize(28.0);
                titleR.setFontColor(Color.BLACK);

                byte[] imageData = Files.readAllBytes(architectureImage.toPath());
                XSLFPictureData pictureData = ppt.addPicture(imageData, XSLFPictureData.PictureType.PNG);
                XSLFPictureShape picture = slide.createPicture(pictureData);
                picture.setAnchor(new Rectangle(50, 100, 500, 350));
                logger.debug("Added Architecture (Block Diagram) slide");
            }

            // 如果是 full PPT，添加其他幻灯片
            if ("full".equals(pptType)) {
                // 添加 Pinout & Pin Function 幻灯片（合并 PIN CONFIGURATION 和 PIN DESCRIPTION）
                if (imagePaths.size() >= 2 && new File(imagePaths.get(0)).exists() && new File(imagePaths.get(1)).exists()) {
                    XSLFSlide slide = ppt.createSlide(layout);
                    XSLFTextShape title = slide.getPlaceholder(0);
                    title.clearText();
                    XSLFTextParagraph titleP = title.addNewTextParagraph();
                    XSLFTextRun titleR = titleP.addNewTextRun();
                    titleR.setText(SLIDE_TITLES[2]); // Pinout & Pin Function
                    titleR.setFontSize(28.0);
                    titleR.setFontColor(Color.BLACK);

                    // 添加 PIN CONFIGURATION 图片
                    byte[] configImageData = Files.readAllBytes(new File(imagePaths.get(0)).toPath());
                    XSLFPictureData configPictureData = ppt.addPicture(configImageData, XSLFPictureData.PictureType.PNG);
                    XSLFPictureShape configPicture = slide.createPicture(configPictureData);
                    configPicture.setAnchor(new Rectangle(50, 100, 500, 175)); // 上半部分

                    // 添加 PIN DESCRIPTION 图片
                    byte[] descImageData = Files.readAllBytes(new File(imagePaths.get(1)).toPath());
                    XSLFPictureData descPictureData = ppt.addPicture(descImageData, XSLFPictureData.PictureType.PNG);
                    XSLFPictureShape descPicture = slide.createPicture(descPictureData);
                    descPicture.setAnchor(new Rectangle(50, 300, 500, 175)); // 下半部分
                    logger.debug("Added Pinout & Pin Function slide");
                } else {
                    logger.warn("Missing images for Pinout & Pin Function");
                }

                // 添加 Typical Application Circuit 幻灯片
                if (imagePaths.size() >= 3 && new File(imagePaths.get(2)).exists()) {
                    XSLFSlide slide = ppt.createSlide(layout);
                    XSLFTextShape title = slide.getPlaceholder(0);
                    title.clearText();
                    XSLFTextParagraph titleP = title.addNewTextParagraph();
                    XSLFTextRun titleR = titleP.addNewTextRun();
                    titleR.setText(SLIDE_TITLES[3]);
                    titleR.setFontSize(28.0);
                    titleR.setFontColor(Color.BLACK);

                    byte[] imageData = Files.readAllBytes(new File(imagePaths.get(2)).toPath());
                    XSLFPictureData pictureData = ppt.addPicture(imageData, XSLFPictureData.PictureType.PNG);
                    XSLFPictureShape picture = slide.createPicture(pictureData);
                    picture.setAnchor(new Rectangle(50, 100, 500, 350));
                    logger.debug("Added Typical Application Circuit slide");
                } else {
                    logger.warn("Missing image for Typical Application Circuit");
                }

                // 添加 EC SPEC 幻灯片
                if (imagePaths.size() >= 4 && new File(imagePaths.get(3)).exists()) {
                    XSLFSlide slide = ppt.createSlide(layout);
                    XSLFTextShape title = slide.getPlaceholder(0);
                    title.clearText();
                    XSLFTextParagraph titleP = title.addNewTextParagraph();
                    XSLFTextRun titleR = titleP.addNewTextRun();
                    titleR.setText(SLIDE_TITLES[4]);
                    titleR.setFontSize(28.0);
                    titleR.setFontColor(Color.BLACK);

                    byte[] imageData = Files.readAllBytes(new File(imagePaths.get(3)).toPath());
                    XSLFPictureData pictureData = ppt.addPicture(imageData, XSLFPictureData.PictureType.PNG);
                    XSLFPictureShape picture = slide.createPicture(pictureData);
                    picture.setAnchor(new Rectangle(50, 100, 500, 350));
                    logger.debug("Added EC SPEC slide");
                } else {
                    logger.warn("Missing image for EC SPEC");
                }

                // 添加 Package & Pad 幻灯片
                if (packagePadImage != null && packagePadImage.exists() && packagePadImage.length() > 0) {
                    XSLFSlide slide = ppt.createSlide(layout);
                    XSLFTextShape title = slide.getPlaceholder(0);
                    title.clearText();
                    XSLFTextParagraph titleP = title.addNewTextParagraph();
                    XSLFTextRun titleR = titleP.addNewTextRun();
                    titleR.setText(SLIDE_TITLES[5]);
                    titleR.setFontSize(28.0);
                    titleR.setFontColor(Color.BLACK);

                    byte[] imageData = Files.readAllBytes(packagePadImage.toPath());
                    XSLFPictureData pictureData = ppt.addPicture(imageData, XSLFPictureData.PictureType.PNG);
                    XSLFPictureShape picture = slide.createPicture(pictureData);
                    picture.setAnchor(new Rectangle(50, 100, 500, 350));
                    logger.debug("Added Package & Pad slide");
                } else {
                    logger.warn("Missing Package & Pad image");
                }
            }

            if (ppt.getSlides().isEmpty()) {
                logger.warn("No slides generated for PPT");
                Response response = Response.builder().status("failed").msg("No slides generated").build();
                responsePrint(resp, gson.toJson(response));
                return;
            }

            File tempPpt = new File(uploadPath.toFile(), "temp_ppt_" + System.currentTimeMillis() + ".pptx");
            try (FileOutputStream fos = new FileOutputStream(tempPpt)) {
                ppt.write(fos);
                logger.info("Temporary PPT saved to: {} (size: {} bytes)", tempPpt.getAbsolutePath(), tempPpt.length());
            }

            resp.setContentType("application/vnd.openxmlformats-officedocument.presentationml.presentation");
            resp.setHeader("Content-Disposition", "attachment; filename=" + ("full".equals(pptType) ? "FullPinDiagrams.pptx" : "PartialPinDiagrams.pptx"));

            try (OutputStream out = resp.getOutputStream()) {
                ppt.write(out);
                out.flush();
                logger.info("PPT sent to client (size: {} slides)", ppt.getSlides().size());
            }

            ppt.close();
        } catch (Exception e) {
            logger.error("PPT generation failed", e);
            Response response = Response.builder().status("failed").msg("PPT generation failed: " + e.getMessage()).build();
            try {
                responsePrint(resp, gson.toJson(response));
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        } finally {
            imagePaths.forEach(path -> {
                File img = new File(path);
                if (img.exists()) {
                    try {
                        Files.deleteIfExists(img.toPath());
                        logger.debug("Deleted image: {}", path);
                    } catch (IOException e) {
                        logger.warn("Failed to delete image: {}", path, e);
                    }
                }
            });
        }
    }

    private String generateProductOverview(File pdfFile) {
        logger.info("Generating Product Overview for file: {}", pdfFile.getName());
        try {
            Response markdownResponse = fileService.toMarkdown(pdfFile);
            if (markdownResponse == null || !"success".equals(markdownResponse.getStatus())) {
                String errorMsg = markdownResponse != null ? markdownResponse.getMsg() : "未知错误";
                logger.error("PDF 转换 Markdown 失败: {}", errorMsg);
                return null;
            }
            String markdownContent = markdownResponse.getData().toString();
            logger.info("Markdown 内容长度: {}", markdownContent.length());

            String prompt = "Extract the following information from the provided content and return it in plain text format with exactly the following structure, with no additional text, headings, or Markdown formatting. If any information is missing, use 'N/A'.:\n" +
                    "1. Device: [Device Name]\n" +
                    "2. Part No.: [Part Number]\n" +
                    "3. Process: [Process]\n" +
                    "4. Power supply: [Power Supply]\n" +
                    "5. Die Size (including scribe line 60 um): [Die Size]\n" +
                    "6. Temperature Range: [Temperature Range]\n" +
                    "7. Estimated Gross Die per wafer: [Gross Die]\n" +
                    "8. Package: [Package]\n" +
                    "9. Auto or Non-Auto: [Auto Grade]\n" +
                    "10. ISO26262 ASIL: [ASIL Level]";

            List<String> chunks = splitContentIntoChunks(markdownContent);
            List<List<String>> history = new ArrayList<>();
            StringBuilder finalSummary = new StringBuilder();

            for (int i = 0; i < chunks.size(); i++) {
                String chunkPrompt = "This is chunk " + (i + 1) + " of " + chunks.size() + ":\n" +
                        "-------------------------------------\n" +
                        chunks.get(i) +
                        "-------------------------------------\n" +
                        (i == chunks.size() - 1 ? "This is the final chunk. Return the complete Product Overview in plain text format, strictly following the specified structure with no additional text or formatting." : "Analyze this chunk and provide insights or a partial summary.");

                ChatCompletionResult result = callLLm(prompt, history, chunkPrompt);
                if (result == null || result.getChoices() == null || result.getChoices().isEmpty()) {
                    logger.error("AI 处理第 {}/{} 块分块失败", i + 1, chunks.size());
                    return null;
                }

                String aiResponse = result.getChoices().get(0).getMessage().getContent();
                history.add(Lists.newArrayList(chunkPrompt, aiResponse));

                if (i == chunks.size() - 1) {
                    // 清理可能的 Markdown 格式或多余内容
                    String cleanedResponse = aiResponse.replaceAll("(?m)^#.*$|^-.*$|^\\*.*$|^\\s*\\n", "").trim();
                    // 确保只保留 10 项格式
                    StringBuilder filteredResponse = new StringBuilder();
                    String[] lines = cleanedResponse.split("\n");
                    for (String line : lines) {
                        if (line.matches("^\\d+\\.\\s*[^:]+:.*$")) {
                            filteredResponse.append(line).append("\n");
                        }
                    }
                    finalSummary.append(filteredResponse);
                }
            }

            return finalSummary.toString().trim();
        } catch (Exception e) {
            logger.error("Product Overview generation failed", e);
            return null;
        }
    }

    private List<String> splitContentIntoChunks(String content) {
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
            start = end + 1;
        }
        return chunks;
    }

    private ChatCompletionResult callLLm(String prompt, List<List<String>> history, String userMsg) {
        ChatCompletionRequest request = new ChatCompletionRequest();
        List<ChatMessage> chatMessages = new ArrayList<>();

        ChatMessage systemMessage = new ChatMessage();
        systemMessage.setContent(prompt);
        systemMessage.setRole("system");
        chatMessages.add(systemMessage);

        for (List<String> entry : history) {
            ChatMessage userMessage = new ChatMessage();
            userMessage.setRole("user");
            userMessage.setContent(entry.get(0));

            ChatMessage assistantMessage = new ChatMessage();
            assistantMessage.setRole("assistant");
            assistantMessage.setContent(entry.get(1));

            chatMessages.add(userMessage);
            chatMessages.add(assistantMessage);
        }

        ChatMessage userMessage = new ChatMessage();
        userMessage.setRole("user");
        userMessage.setContent(userMsg);
        chatMessages.add(userMessage);

        request.setMax_tokens(16384);
        request.setTemperature(0.2);
        request.setMessages(chatMessages);

        return completionsService.completions(request);
    }

    private void queryImage(File pdfFile, String keyword, HttpServletResponse resp) {
        logger.info("Querying image for keyword: {}", keyword);
        try {
            String imagePath = callPythonService(pdfFile, keyword);
            if (imagePath == null || !new File(imagePath).exists() || new File(imagePath).length() == 0) {
                logger.warn("No valid image found for keyword: {}", keyword);
                Response response = Response.builder().status("failed").msg("No image found for keyword").build();
                responsePrint(resp, gson.toJson(response));
                return;
            }

            byte[] imageData = Files.readAllBytes(new File(imagePath).toPath());
            String base64Image = java.util.Base64.getEncoder().encodeToString(imageData);
            Response response = Response.builder().status("success").data(base64Image).build();
            responsePrint(resp, gson.toJson(response));
            logger.info("Image queried and sent to client (size: {} bytes)", imageData.length);

            Files.deleteIfExists(new File(imagePath).toPath());
            logger.debug("Deleted image: {}", imagePath);
        } catch (Exception e) {
            logger.error("Image query failed", e);
            Response response = Response.builder().status("failed").msg("Image query failed: " + e.getMessage()).build();
            try {
                responsePrint(resp, gson.toJson(response));
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    private String callPythonService(File pdfFile, String keyword) throws IOException {
        logger.info("Calling Python service for keyword: {}", keyword);
        String boundary = "---------------------------" + System.currentTimeMillis();
        String LINE_FEED = "\r\n";
        URL url = new URL("http://127.0.0.1:8124/screenshot");
        HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
        httpConn.setUseCaches(false);
        httpConn.setDoOutput(true);
        httpConn.setDoInput(true);
        httpConn.setRequestMethod("POST");
        httpConn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

        Path uploadPath = Paths.get(getServletContext().getRealPath("/") + File.separator + UPLOAD_DIR);
        Path debugRequestPath = uploadPath.resolve("debug_request_" + System.currentTimeMillis() + ".txt");

        try (OutputStream outputStream = new BufferedOutputStream(httpConn.getOutputStream());
             PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream, "UTF-8"), true);
             FileOutputStream debugStream = new FileOutputStream(debugRequestPath.toFile())) {

            StringBuilder debugContent = new StringBuilder();

            String keywordPart = "--" + boundary + LINE_FEED +
                    "Content-Disposition: form-data; name=\"keyword\"" + LINE_FEED +
                    LINE_FEED + keyword + LINE_FEED;
            writer.append(keywordPart);
            debugContent.append(keywordPart);

            String filePartHeader = "--" + boundary + LINE_FEED +
                    "Content-Disposition: form-data; name=\"file\"; filename=\"" + pdfFile.getName() + "\"" + LINE_FEED +
                    "Content-Type: application/pdf" + LINE_FEED +
                    LINE_FEED;
            writer.append(filePartHeader);
            debugContent.append(filePartHeader);

            writer.flush();
            outputStream.flush();
            debugStream.write(debugContent.toString().getBytes("UTF-8"));

            Files.copy(pdfFile.toPath(), outputStream);
            Files.copy(pdfFile.toPath(), debugStream);
            outputStream.flush();
            debugStream.flush();

            String endBoundary = LINE_FEED + "--" + boundary + "--" + LINE_FEED;
            writer.append(endBoundary);
            writer.flush();
            debugContent.append(endBoundary);
            debugStream.write(endBoundary.getBytes("UTF-8"));

            logger.info("Debug request saved to: {}", debugRequestPath);

            int responseCode = httpConn.getResponseCode();
            logger.info("Python service response code: {}", responseCode);

            if (responseCode != 200) {
                String errorResponse = "";
                try (BufferedReader errorIn = new BufferedReader(new InputStreamReader(httpConn.getErrorStream()))) {
                    StringBuilder errorBuilder = new StringBuilder();
                    String errorLine;
                    while ((errorLine = errorIn.readLine()) != null) {
                        errorBuilder.append(errorLine);
                    }
                    errorResponse = errorBuilder.toString();
                    logger.error("Python service error response: {}", errorResponse);
                } catch (IOException e) {
                    logger.warn("Failed to read error response", e);
                }
                throw new IOException("Python service returned non-200 status: " + responseCode + ", error: " + errorResponse);
            }

            try (BufferedReader in = new BufferedReader(new InputStreamReader(httpConn.getInputStream()))) {
                StringBuilder response = new StringBuilder();
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                String jsonResponse = response.toString();
                logger.debug("Python service response: {}", jsonResponse);

                Response serviceResponse = gson.fromJson(jsonResponse, Response.class);
                if (serviceResponse.getScreenshot_path() != null) {
                    String imagePath = serviceResponse.getScreenshot_path();
                    logger.info("Image path received: {}", imagePath);
                    return imagePath;
                } else {
                    logger.warn("Python service failed for keyword '{}': {}", keyword, serviceResponse.getError());
                    return null;
                }
            }
        } finally {
            httpConn.disconnect();
        }
    }
}
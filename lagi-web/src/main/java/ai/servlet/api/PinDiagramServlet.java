package ai.servlet.api;

import ai.common.pojo.Response;
import ai.llm.service.CompletionsService;
import ai.openai.pojo.ChatCompletionRequest;
import ai.openai.pojo.ChatCompletionResult;
import ai.openai.pojo.ChatMessage;
import ai.servlet.BaseServlet;
import ai.vector.FileService;
import com.amazonaws.util.IOUtils;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.poi.sl.usermodel.TextParagraph;
import org.apache.poi.xslf.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.awt.geom.Rectangle2D;
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
    private static final String TEMPLATE_DIR = "templates";
    private static final Logger logger = LoggerFactory.getLogger(PinDiagramServlet.class);
    private final Gson gson = new Gson();
    private final FileService fileService = new FileService();
    private final CompletionsService completionsService = new CompletionsService();
    private static final int CHUNK_SIZE = 20000;
    private static final String[] KEYWORDS = {
            "PIN CONFIGURATION",
            "PIN DESCRIPTION",
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
/*            // 1. 加载封面模板
            String coverTemplateName = "full".equals(pptType) ? "designreview_cover.pptx" : "kickoff_cover.pptx";
            InputStream coverInputStream = getClass().getClassLoader().getResourceAsStream("templates/" + coverTemplateName);*/

            // 2. 创建新PPT并设置目标尺寸
            XMLSlideShow ppt = new XMLSlideShow();
            Dimension targetSize = new Dimension(1280, 720); // 目标尺寸
            ppt.setPageSize(targetSize);

            /*// 在generatePPT方法中修改封面页处理部分
            if (coverInputStream != null) {
                XMLSlideShow coverPPT = new XMLSlideShow(coverInputStream);
                XSLFSlide coverSlide = coverPPT.getSlides().get(0);

                Dimension originalSize = coverPPT.getPageSize();
                double widthRatio = targetSize.getWidth() / originalSize.getWidth();
                double heightRatio = targetSize.getHeight() / originalSize.getHeight();

                XSLFSlide importedCover = ppt.createSlide();

                // 加载并应用封面背景图
                InputStream coverBgInputStream = getClass().getClassLoader().getResourceAsStream("templates/Cover_image.png");
                if (coverBgInputStream == null) {
                    logger.error("Cover background image not found: templates/Cover_image.png");
                    throw new IOException("Cover background image not found");
                }
                byte[] coverBgImageData = IOUtils.toByteArray(coverBgInputStream);
                coverBgInputStream.close();
                XSLFPictureData coverBgPictureData = ppt.addPicture(coverBgImageData, XSLFPictureData.PictureType.PNG);
                applyBackground(importedCover, coverBgPictureData); // 使用优化后的 applyBackground

                importedCover.importContent(coverSlide); // 在背景图后导入文字内容
                resizeSlideContent(importedCover, widthRatio, heightRatio);
                coverInputStream.close();
                coverPPT.close();
            }*/

            // 2. 设置PPT尺寸为宽屏（16:9比例）
            ppt.setPageSize(new java.awt.Dimension(1280, 720)); // 宽度1280，高度720

            // 3. 加载背景图片（用于内容页）
            InputStream backgroundInputStream = getClass().getClassLoader().getResourceAsStream("templates/background.png");
            if (backgroundInputStream == null) {
                logger.error("Background image not found in resources: templates/background.png");
                throw new IOException("Background image not found");
            }
            byte[] bgImageData = IOUtils.toByteArray(backgroundInputStream);
            backgroundInputStream.close();
            XSLFPictureData bgPictureData = ppt.addPicture(bgImageData, XSLFPictureData.PictureType.PNG);

            // 获取母版和布局
            XSLFSlideMaster master = ppt.getSlideMasters().get(0);
            XSLFSlideLayout layout = master.getLayout(SlideLayout.TITLE_AND_CONTENT);

            // 生成 Product Overview
            String productOverview = generateProductOverview(pdfFile);
            if (productOverview == null || productOverview.trim().isEmpty()) {
                logger.warn("Failed to generate Product Overview");
            }

            // 收集截图
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

            // 添加 Product Overview 幻灯片
            if (productOverview != null && !productOverview.trim().isEmpty()) {
                XSLFSlide slide = ppt.createSlide(layout);
                applyBackground(slide, bgPictureData);
                clearPlaceholders(slide);

                XSLFTextShape title = slide.createTextBox();
                title.setAnchor(new Rectangle(50, 40, 800, 50)); // 调整标题位置
                XSLFTextParagraph titleP = title.addNewTextParagraph();
                XSLFTextRun titleR = titleP.addNewTextRun();
                titleR.setText(SLIDE_TITLES[0]);
                titleR.setFontFamily("Calibri");
                titleR.setFontSize(24.0);
                titleR.setFontColor(Color.BLACK);
                titleR.setBold(true);

                XSLFTextShape content = slide.createTextBox();
                content.setAnchor(new Rectangle(50, 100, 1000, 500)); // 调整内容区域宽度
                String[] lines = productOverview.split("\n");
                for (String line : lines) {
                    XSLFTextParagraph p = content.addNewTextParagraph();
                    XSLFTextRun r = p.addNewTextRun();
                    r.setText(line);
                    r.setFontSize(20.0);
                    r.setFontColor(Color.BLACK);
                }
                addSlideNumber(slide, ppt.getSlides().size());
            }

            // 添加 Architecture (Block Diagram) 幻灯片
            if (architectureImage != null && architectureImage.exists() && architectureImage.length() > 0) {
                XSLFSlide slide = ppt.createSlide(layout);
                applyBackground(slide, bgPictureData);
                clearPlaceholders(slide);

                XSLFTextShape title = slide.createTextBox();
                title.setAnchor(new Rectangle(50, 40, 800, 50));
                XSLFTextParagraph titleP = title.addNewTextParagraph();
                XSLFTextRun titleR = titleP.addNewTextRun();
                titleR.setText(SLIDE_TITLES[1]);
                titleR.setFontFamily("Calibri");
                titleR.setFontSize(24.0);
                titleR.setFontColor(Color.BLACK);
                titleR.setBold(true);

                byte[] imageData = Files.readAllBytes(architectureImage.toPath());
                XSLFPictureData pictureData = ppt.addPicture(imageData, XSLFPictureData.PictureType.PNG);
                XSLFPictureShape picture = slide.createPicture(pictureData);
                picture.setAnchor(new Rectangle(50, 100, 1000, 500)); // 调整图片区域宽度
                addSlideNumber(slide, ppt.getSlides().size());
            }

            // 如果是 full PPT，添加其他幻灯片
            if ("full".equals(pptType)) {
                // 添加 Pinout & Pin Function 幻灯片
                if (imagePaths.size() >= 2 && new File(imagePaths.get(0)).exists() && new File(imagePaths.get(1)).exists()) {
                    XSLFSlide slide = ppt.createSlide(layout);
                    applyBackground(slide, bgPictureData);
                    clearPlaceholders(slide);

                    XSLFTextShape title = slide.createTextBox();
                    title.setAnchor(new Rectangle(50, 40, 800, 50));
                    XSLFTextParagraph titleP = title.addNewTextParagraph();
                    XSLFTextRun titleR = titleP.addNewTextRun();
                    titleR.setText(SLIDE_TITLES[2]);
                    titleR.setFontFamily("Calibri");
                    titleR.setFontSize(24.0);
                    titleR.setFontColor(Color.BLACK);
                    titleR.setBold(true);

                    byte[] configImageData = Files.readAllBytes(new File(imagePaths.get(0)).toPath());
                    XSLFPictureData configPictureData = ppt.addPicture(configImageData, XSLFPictureData.PictureType.PNG);
                    XSLFPictureShape configPicture = slide.createPicture(configPictureData);
                    configPicture.setAnchor(new Rectangle(50, 100, 1000, 250));

                    byte[] descImageData = Files.readAllBytes(new File(imagePaths.get(1)).toPath());
                    XSLFPictureData descPictureData = ppt.addPicture(descImageData, XSLFPictureData.PictureType.PNG);
                    XSLFPictureShape descPicture = slide.createPicture(descPictureData);
                    descPicture.setAnchor(new Rectangle(50, 360, 1000, 250));
                    addSlideNumber(slide, ppt.getSlides().size());
                }

                // 添加 Typical Application Circuit 幻灯片
                if (imagePaths.size() >= 3 && new File(imagePaths.get(2)).exists()) {
                    XSLFSlide slide = ppt.createSlide(layout);
                    applyBackground(slide, bgPictureData);
                    clearPlaceholders(slide);

                    XSLFTextShape title = slide.createTextBox();
                    title.setAnchor(new Rectangle(50, 40, 800, 50));
                    XSLFTextParagraph titleP = title.addNewTextParagraph();
                    XSLFTextRun titleR = titleP.addNewTextRun();
                    titleR.setText(SLIDE_TITLES[3]);
                    titleR.setFontFamily("Calibri");
                    titleR.setFontSize(24.0);
                    titleR.setFontColor(Color.BLACK);
                    titleR.setBold(true);

                    byte[] imageData = Files.readAllBytes(new File(imagePaths.get(2)).toPath());
                    XSLFPictureData pictureData = ppt.addPicture(imageData, XSLFPictureData.PictureType.PNG);
                    XSLFPictureShape picture = slide.createPicture(pictureData);
                    picture.setAnchor(new Rectangle(50, 100, 1000, 500));
                    addSlideNumber(slide, ppt.getSlides().size());
                }

                // 添加 EC SPEC 幻灯片
                if (imagePaths.size() >= 4 && new File(imagePaths.get(3)).exists()) {
                    XSLFSlide slide = ppt.createSlide(layout);
                    applyBackground(slide, bgPictureData);
                    clearPlaceholders(slide);

                    XSLFTextShape title = slide.createTextBox();
                    title.setAnchor(new Rectangle(50, 40, 800, 50));
                    XSLFTextParagraph titleP = title.addNewTextParagraph();
                    XSLFTextRun titleR = titleP.addNewTextRun();
                    titleR.setText(SLIDE_TITLES[4]);
                    titleR.setFontFamily("Calibri");
                    titleR.setFontSize(24.0);
                    titleR.setFontColor(Color.BLACK);
                    titleR.setBold(true);

                    byte[] imageData = Files.readAllBytes(new File(imagePaths.get(3)).toPath());
                    XSLFPictureData pictureData = ppt.addPicture(imageData, XSLFPictureData.PictureType.PNG);
                    XSLFPictureShape picture = slide.createPicture(pictureData);
                    picture.setAnchor(new Rectangle(50, 100, 1000, 500));
                    addSlideNumber(slide, ppt.getSlides().size());
                }

                // 添加 Package & Pad 幻灯片
                if (packagePadImage != null && packagePadImage.exists() && packagePadImage.length() > 0) {
                    XSLFSlide slide = ppt.createSlide(layout);
                    applyBackground(slide, bgPictureData);
                    clearPlaceholders(slide);

                    XSLFTextShape title = slide.createTextBox();
                    title.setAnchor(new Rectangle(50, 40, 800, 50));
                    XSLFTextParagraph titleP = title.addNewTextParagraph();
                    XSLFTextRun titleR = titleP.addNewTextRun();
                    titleR.setText(SLIDE_TITLES[5]);
                    titleR.setFontFamily("Calibri");
                    titleR.setFontSize(24.0);
                    titleR.setFontColor(Color.BLACK);
                    titleR.setBold(true);

                    byte[] imageData = Files.readAllBytes(packagePadImage.toPath());
                    XSLFPictureData pictureData = ppt.addPicture(imageData, XSLFPictureData.PictureType.PNG);
                    XSLFPictureShape picture = slide.createPicture(pictureData);
                    picture.setAnchor(new Rectangle(50, 100, 1000, 500));
                    addSlideNumber(slide, ppt.getSlides().size());
                }
            }

            // 输出PPT文件
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

    /**
     * 调整幻灯片中所有元素的大小和位置
     */
    private void resizeSlideContent(XSLFSlide slide, double widthRatio, double heightRatio) {
        for (XSLFShape shape : slide.getShapes()) {
            // 获取原始位置和大小
            Rectangle2D originalAnchor = shape.getAnchor();

            // 创建新的位置和大小（使用XSLFShape.setAnchor()的正确替代方案）
            Rectangle2D newAnchor = new Rectangle2D.Double(
                    originalAnchor.getX() * widthRatio,
                    originalAnchor.getY() * heightRatio,
                    originalAnchor.getWidth() * widthRatio,
                    originalAnchor.getHeight() * heightRatio
            );

            // 正确的设置方法（根据形状类型处理）
            if (shape instanceof XSLFTextBox) {
                ((XSLFTextBox)shape).setAnchor(newAnchor);
            }
            else if (shape instanceof XSLFPictureShape) {
                ((XSLFPictureShape)shape).setAnchor(newAnchor);
            }
            else if (shape instanceof XSLFAutoShape) {
                ((XSLFAutoShape)shape).setAnchor(newAnchor);
            }
            // 其他形状类型...

            // 处理文本框字体大小
            if (shape instanceof XSLFTextShape) {
                XSLFTextShape textShape = (XSLFTextShape) shape;
                for (XSLFTextParagraph paragraph : textShape.getTextParagraphs()) {
                    for (XSLFTextRun textRun : paragraph.getTextRuns()) {
                        double originalSize = textRun.getFontSize();
                        if (originalSize > 0) {  // 只调整已设置的字体大小
                            textRun.setFontSize(originalSize * Math.min(widthRatio, heightRatio));
                        }
                    }
                }
            }
        }
    }

    // 应用背景图片到幻灯片
    private void applyBackground(XSLFSlide slide, XSLFPictureData bgPictureData) {
        // 清除原有背景
        XSLFBackground bg = slide.getBackground();
        bg.setFillColor(Color.WHITE);

        // 创建背景图片并覆盖整个幻灯片
        XSLFPictureShape bgPicture = slide.createPicture(bgPictureData);
        Dimension pageSize = slide.getSlideShow().getPageSize();
        bgPicture.setAnchor(new Rectangle(0, 0, pageSize.width, pageSize.height));

        // 替代moveToBack()的方案：确保背景是第一个添加的形状（默认会在最底层）
        // 不需要额外操作，因为createPicture()新创建的形状默认在现有形状之上
        // 但由于我们先添加背景，再添加其他内容，所以背景自然会在最底层
    }

    // 清除幻灯片上的占位符文本
    private void clearPlaceholders(XSLFSlide slide) {
        for (XSLFShape shape : slide.getShapes()) {
            if (shape instanceof XSLFTextShape) {
                XSLFTextShape textShape = (XSLFTextShape) shape;
                // 修改判断方式，使用更可靠的方法检测占位符
                if (textShape.getPlaceholder() != null) {
                    textShape.clearText();
                }
            }
        }
    }

    private void addSlideNumber(XSLFSlide slide, int slideNumber) {
//        if (slideNumber > 1) {
            XSLFTextBox textBox = slide.createTextBox();
            textBox.setAnchor(new Rectangle(1128, 650, 100, 40)); // 右下角位置
            XSLFTextParagraph paragraph = textBox.addNewTextParagraph();
            paragraph.setTextAlign(TextParagraph.TextAlign.RIGHT);
            XSLFTextRun textRun = paragraph.addNewTextRun();
            textRun.setText(String.valueOf(slideNumber));
            textRun.setFontFamily("Arial");
            textRun.setFontSize(9.0);
            textRun.setFontColor(Color.BLACK);
            textRun.setBold(false);
//        }
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
                    String cleanedResponse = aiResponse.replaceAll("(?m)^#.*$|^-.*$|^\\*.*$|^\\s*\\n", "").trim();
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
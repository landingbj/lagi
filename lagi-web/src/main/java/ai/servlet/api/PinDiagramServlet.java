package ai.servlet.api;

import ai.common.pojo.Response;
import ai.servlet.BaseServlet;
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
    private static final String[] KEYWORDS = {
            "PIN CONFIGURATION",
            "PIN DESCRIPTION",
            "Typical Application Circuit",
            "ELECTRICAL CHARACTERISTICS"
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
        File pdfFile = null;

        try {
            List<FileItem> items = upload.parseRequest(req);
            logger.info("Number of form items: {}", items.size());

            for (FileItem item : items) {
                if (item.isFormField()) {
                    String fieldName = item.getFieldName();
                    String fieldValue = item.getString("UTF-8");
                    if ("keyword".equals(fieldName)) {
                        keyword = fieldValue;
                    }
                    logger.debug("Form field: {} = {}", fieldName, fieldValue);
                } else {
                    String fileName = item.getName();
                    if (fileName != null && !fileName.isEmpty()) {
                        fileName = fileName.replaceAll("[^a-zA-Z0-9.-]", "_");
                        fileName = System.currentTimeMillis() + "_" + fileName;
                        pdfFile = new File(uploadPath.toFile(), fileName);
                        item.write(pdfFile);
                        logger.info("Uploaded file saved: {} (size: {} bytes)", pdfFile.getAbsolutePath(), pdfFile.length());
                    }
                }
            }

            if (pdfFile == null || !pdfFile.exists()) {
                logger.warn("No file uploaded or file not saved");
                Response response = Response.builder().status("failed").msg("No file uploaded").build();
                responsePrint(resp, gson.toJson(response));
                return;
            }

            if (keyword != null && !keyword.trim().isEmpty()) {
                queryImage(pdfFile, keyword, resp);
            } else {
                generatePPT(pdfFile, resp);
            }
        } catch (Exception e) {
            logger.error("Request processing failed", e);
            Response response = Response.builder().status("failed").msg("Request processing failed: " + e.getMessage()).build();
            responsePrint(resp, gson.toJson(response));
        } finally {
            if (pdfFile != null && pdfFile.exists()) {
                logger.info("Temporary file retained for debugging: {}", pdfFile.getAbsolutePath());
            }
        }
    }

    private void generatePPT(File pdfFile, HttpServletResponse resp) {
        logger.info("Generating PPT for file: {}", pdfFile.getName());
        List<String> imagePaths = new ArrayList<>();
        Path uploadPath = Paths.get(getServletContext().getRealPath("/") + File.separator + UPLOAD_DIR);

        try {
            for (int i = 0; i < KEYWORDS.length; i++) {
                String keyword = KEYWORDS[i];
                String imagePath = callPythonService(pdfFile, keyword);
                if (imagePath != null && new File(imagePath).exists() && new File(imagePath).length() > 0) {
                    // Use unique debug image name with index
                    String debugImageName = "debug_" + i + "_" + keyword.replaceAll("[^a-zA-Z0-9]", "_") + ".png";
                    Files.copy(Paths.get(imagePath), uploadPath.resolve(debugImageName), StandardCopyOption.REPLACE_EXISTING);
                    logger.info("Debug image saved: {}", uploadPath.resolve(debugImageName));
                    imagePaths.add(imagePath);
                    logger.info("Image generated for keyword '{}': {} (size: {} bytes)", keyword, imagePath, new File(imagePath).length());
                } else {
                    logger.warn("No valid image for keyword '{}'", keyword);
                }
            }

            if (imagePaths.isEmpty()) {
                logger.warn("No images generated for PPT");
                Response response = Response.builder().status("failed").msg("No images generated").build();
                responsePrint(resp, gson.toJson(response));
                return;
            }

            XMLSlideShow ppt = new XMLSlideShow();
            XSLFSlideMaster master = ppt.getSlideMasters().get(0);
            XSLFSlideLayout layout = master.getLayout(SlideLayout.TITLE_AND_CONTENT);

            for (int i = 0; i < imagePaths.size(); i++) {
                String imagePath = imagePaths.get(i);
                String keyword = KEYWORDS[i];

                XSLFSlide slide = ppt.createSlide(layout);
                XSLFTextShape title = slide.getPlaceholder(0);
                title.clearText();
                XSLFTextParagraph titleP = title.addNewTextParagraph();
                XSLFTextRun titleR = titleP.addNewTextRun();
                titleR.setText(keyword);
                titleR.setFontSize(28.0);
                titleR.setFontColor(Color.BLACK);

                File imageFile = new File(imagePath);
                if (!imageFile.exists() || imageFile.length() == 0) {
                    logger.warn("Image file invalid: {}", imagePath);
                    continue;
                }

                byte[] imageData = Files.readAllBytes(imageFile.toPath());
                XSLFPictureData pictureData = ppt.addPicture(imageData, XSLFPictureData.PictureType.PNG);
                XSLFPictureShape picture = slide.createPicture(pictureData);
                picture.setAnchor(new Rectangle(50, 100, 500, 350));
                logger.debug("Added slide for keyword '{}'", keyword);
            }

            File tempPpt = new File(uploadPath.toFile(), "temp_ppt_" + System.currentTimeMillis() + ".pptx");
            try (FileOutputStream fos = new FileOutputStream(tempPpt)) {
                ppt.write(fos);
                logger.info("Temporary PPT saved to: {} (size: {} bytes)", tempPpt.getAbsolutePath(), tempPpt.length());
            }

            resp.setContentType("application/vnd.openxmlformats-officedocument.presentationml.presentation");
            resp.setHeader("Content-Disposition", "attachment; filename=PinDiagrams.pptx");

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
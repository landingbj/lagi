package ai.servlet.api;

import java.io.File;
import java.io.IOException;
import java.util.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ai.common.pojo.ImageGenerationRequest;
import ai.common.pojo.ImageGenerationResult;
import ai.image.service.AllImageService;
import ai.ocr.OcrService;
import ai.servlet.BaseServlet;
import ai.translate.TranslateService;
import ai.utils.AiGlobal;
import ai.utils.MigrateGlobal;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImageApiServlet extends BaseServlet {
    private static final Logger logger = LoggerFactory.getLogger(ImageApiServlet.class);
    private static final long serialVersionUID = 1L;
    private final AllImageService imageService = new AllImageService();
    private final TranslateService translateService = new TranslateService();
    private final OcrService ocrService = new OcrService();


    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setHeader("Content-Type", "application/json;charset=utf-8");
        String url = req.getRequestURI();
        String method = url.substring(url.lastIndexOf("/") + 1);
        if (method.equals("generations") || method.equals("text2image")) {
            this.generations(req, resp);
        } else if (method.equals("image2ocr")) {
            this.image2ocr(req, resp);
        }
    }

    private void generations(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=utf-8");
        ImageGenerationRequest request = reqBodyToObj(req, ImageGenerationRequest.class);
        String english = translateService.toEnglish(request.getPrompt());
        if (english != null) {
            request.setPrompt(english);
        }
        ImageGenerationResult result = imageService.generations(request);
        responsePrint(resp, toJson(result));
    }


    private void image2ocr(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=utf-8");
        String tempPath = this.getServletContext().getRealPath(AiGlobal.DIR_TEMP);
        File tmpFile = new File(tempPath);
        if (!tmpFile.exists()) {
            tmpFile.mkdir();
        }
        List<File> fileList = new ArrayList<>();
        try {
            DiskFileItemFactory factory = new DiskFileItemFactory();
            ServletFileUpload upload = new ServletFileUpload(factory);
            upload.setHeaderEncoding("UTF-8");
            if (!ServletFileUpload.isMultipartContent(req)) {
                return;
            }
            upload.setFileSizeMax(MigrateGlobal.IMAGE_FILE_SIZE_LIMIT);
            upload.setSizeMax(MigrateGlobal.IMAGE_FILE_SIZE_LIMIT);
            List<?> fileItems = upload.parseRequest(req);
            for (Object fileItem : fileItems) {
                FileItem fi = (FileItem) fileItem;
                if (!fi.isFormField()) {
                    String ext = fi.getName().substring(fi.getName().lastIndexOf("."));
                    String newName = UUID.randomUUID().toString().replace("-", "") + ext;
                    String filePath = tempPath + File.separator + newName;
                    File file = new File(filePath);
                    fi.write(file);
                    fileList.add(file);
                }
            }
        } catch (FileUploadException e) {
            logger.error("Failed to upload file", e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        List<String> ocrResults = ocrService.recognize(fileList);
        Map<String, Object> map = new HashMap<>();
        if (ocrResults == null || ocrResults.isEmpty()) {
            map.put("status", "failed");
        } else {
            map.put("status", "success");
            map.put("data", ocrResults);
        }
        responsePrint(resp, gson.toJson(map));
    }
}

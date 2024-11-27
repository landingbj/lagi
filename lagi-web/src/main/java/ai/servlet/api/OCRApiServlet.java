package ai.servlet.api;

import ai.common.exception.RRException;
import ai.ocr.OcrService;
import ai.servlet.RestfulServlet;
import ai.servlet.annotation.Post;
import ai.utils.MigrateGlobal;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;

@Slf4j
public class OCRApiServlet extends RestfulServlet {

    private static final String UPLOAD_DIR = "/upload";
    private final OcrService ocrService = new OcrService();

    @Post("doc2ocr")
    public List<String> doc2ocr(HttpServletRequest req) throws Exception {
        DiskFileItemFactory factory = new DiskFileItemFactory();
        ServletFileUpload upload = new ServletFileUpload(factory);
        upload.setFileSizeMax(MigrateGlobal.OCR_FILE_SIZE_LIMIT);
        upload.setSizeMax(MigrateGlobal.OCR_FILE_SIZE_LIMIT);
        String uploadDir = getServletContext().getRealPath(UPLOAD_DIR);

        File uploadDirFile = new File(uploadDir);
        if (!uploadDirFile.isDirectory()) {
            uploadDirFile.mkdirs();
        }

        List<?> fileItems = upload.parseRequest(req);
        if (fileItems == null || fileItems.isEmpty()) {
            throw new RRException("Missing files");
        }

        List<File> files = new ArrayList<>();
        List<String> langList = new ArrayList<>();

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
                } while (file.exists());

                try {
                    fi.write(file);
                    files.add(file);
                } catch (Exception e) {
                    log.error("file write error {}", e.getMessage());
                }
            } else {
                String name = fi.getFieldName();
                String value = fi.getString();
                if ("lang".equals(name)) {
                    langList.add(value);
                }
            }
        }
        if (files.isEmpty()) {
            throw new RRException("Missing files");
        }
        if(langList.isEmpty()) {
            langList.add("chn,eng,tai");
        }
        String fileType =  getFileTypeByExtension(files.get(0));
        if("PDF".equals(fileType)) {
            if(files.size() > 1) {
                throw new RRException("only one pdf file is allowed");
            }
            List<String> pdfOcrResult = ocrService.doc2ocr(files.get(0), langList);
            if (pdfOcrResult == null || pdfOcrResult.isEmpty()) {
                throw new RRException("Failed to recognize PDF");
            }
            return pdfOcrResult;
        } else if("Image".equals(fileType)) {
            return ocrService.image2Ocr(files, langList);
        }
        throw new RRException("unsupported file type");
    }

    public static String getFileTypeByExtension(File file) {
        String fileName = file.getName();
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex < 0) {
            return "Unknown";
        }
        String extension = fileName.substring(dotIndex + 1).toLowerCase();
        switch (extension) {
            case "pdf":
                return "PDF";
            case "jpg":
            case "jpeg":
            case "png":
            case "gif":
                return "Image";
            case "doc":
            case "docx":
                return "DOC";
            default:
                return "Unknown";
        }
    }

}

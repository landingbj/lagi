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
import java.util.stream.Collectors;

@Slf4j
public class OCRApiServlet extends RestfulServlet {

    private static final String UPLOAD_DIR = "/upload";
    private final OcrService ocrService = new OcrService();

    @Post("recognize")
    public List<String> recognize(HttpServletRequest req) throws Exception {
        DiskFileItemFactory factory = new DiskFileItemFactory();
        ServletFileUpload upload = new ServletFileUpload(factory);
        upload.setFileSizeMax(MigrateGlobal.IMAGE_FILE_SIZE_LIMIT);
        upload.setSizeMax(MigrateGlobal.IMAGE_FILE_SIZE_LIMIT);
        String uploadDir = getServletContext().getRealPath(UPLOAD_DIR);
        File uploadDirFile = new File(uploadDir);
        if (!uploadDirFile.isDirectory()) {
            uploadDirFile.mkdirs();
        }
        List<?> fileItems = upload.parseRequest(req);
        if (fileItems == null || fileItems.isEmpty()) {
            throw new RRException("缺少文件");
        }
        List<File> files = fileItems.stream().map(fileItem -> {
            FileItem fi = (FileItem) fileItem;
            if (!fi.isFormField()) {
                String fileName = fi.getName();
                File file = null;
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
                    return file;
                } catch (Exception e) {
                    log.error("file write error {}", e.getMessage());
                    return null;
                }
            }
            return null;
        }).collect(Collectors.toList());
        return ocrService.recognize(files);
    }

    @Post("recognizePdf")
    public List<String> recognizePdf(HttpServletRequest req) throws Exception {
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
            throw new RRException("there is no any file");
        }
        List<File> files = new ArrayList<>();
        List<String> langList = new ArrayList<>();
        fileItems.stream().map(fileItem -> {
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
            return null;
        }).collect(Collectors.toList());
        if (files.size() != 1) {
            throw new RRException("only one pdf file is allowed");
        }
        if (langList.isEmpty()) {
            langList.add("chn,eng");
        }
        List<String> pdfOrcResult = ocrService.recognizePdf(files.get(0), langList);
        if (pdfOrcResult == null || pdfOrcResult.isEmpty()) {
            throw new RRException("recognize pdf failed");
        }
        return pdfOrcResult;
    }
}

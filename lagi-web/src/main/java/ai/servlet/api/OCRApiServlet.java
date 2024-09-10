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
import java.util.Date;
import java.util.List;
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
        if(fileItems == null || fileItems.isEmpty()) {
            throw new RRException("缺少文件");
        }
        List<File> files = fileItems.stream().map(fileItem->{
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
}

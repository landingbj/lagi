package ai.servlet.api;

import ai.llm.pojo.InstructionEntity;
import ai.llm.service.InstructionService;
import ai.servlet.BaseServlet;
import ai.utils.MigrateGlobal;
import ai.vector.FileService;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class InstructionApiServlet extends BaseServlet {
    private static final String UPLOAD_DIR = "/upload";
    private final FileService fileService = new FileService();
    private final InstructionService instructionService = new InstructionService();
    private final Logger logger = LoggerFactory.getLogger(InstructionApiServlet.class);

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setHeader("Content-Type", "application/json;charset=utf-8");
        String url = req.getRequestURI();
        String method = url.substring(url.lastIndexOf("/") + 1);
        if (method.equals("generate")) {
            this.generate(req, resp);
        }
    }

    private void generate(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        DiskFileItemFactory factory = new DiskFileItemFactory();
        ServletFileUpload upload = new ServletFileUpload(factory);
        upload.setFileSizeMax(MigrateGlobal.DOC_FILE_SIZE_LIMIT);
        upload.setSizeMax(MigrateGlobal.DOC_FILE_SIZE_LIMIT);
        String uploadDir = getServletContext().getRealPath(UPLOAD_DIR);
        if (!new File(uploadDir).isDirectory()) {
            new File(uploadDir).mkdirs();
        }
        List<File> files = new ArrayList<>();
        Map<String, Object> result = new HashMap<>();
        try {
            List<?> fileItems = upload.parseRequest(req);
            for (Object fileItem : fileItems) {
                FileItem fi = (FileItem) fileItem;
                if (!fi.isFormField()) {
                    String fileName = fi.getName();
                    File file;
                    String newName;
                    do {
                        newName = UUID.randomUUID() + fileName.substring(fileName.lastIndexOf("."));
                        String lastFilePath = uploadDir + File.separator + newName;
                        file = new File(lastFilePath);
                    } while (file.exists());
                    fi.write(file);
                    files.add(file);
                }
            }
        } catch (Exception ex) {
            result.put("msg", "解析文件出现错误");
            logger.error("解析文件出现错误", ex);
        }
        List<InstructionEntity> instructionData = new ArrayList<>();
        if (!files.isEmpty()) {
            for (File file : files) {
                String content = fileService.getFileContent(file);
                if (!StringUtils.isEmpty(content)) {
                    List<InstructionEntity> instructions = instructionService.getInstructionList(content);
                    instructionData.addAll(instructions);
                }
            }
            result.put("status", "success");
            result.put("data", instructionData);
        } else {
            result.put("status", "failed");
        }
        responsePrint(resp, toJson(result));
    }
}

package ai.servlet.api;

import ai.common.pojo.FileChunkResponse;
import ai.common.pojo.Response;
import ai.llm.pojo.InstructionEntity;
import ai.llm.service.InstructionService;
import ai.servlet.BaseServlet;
import ai.utils.MigrateGlobal;
import ai.vector.FileService;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
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

public class DocumentApiServlet extends BaseServlet {
    private static final String UPLOAD_DIR = "/upload";
    private final FileService fileService = new FileService();
    private final Logger logger = LoggerFactory.getLogger(DocumentApiServlet.class);

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setHeader("Content-Type", "application/json;charset=utf-8");
        String url = req.getRequestURI();
        String method = url.substring(url.lastIndexOf("/") + 1);
        if (method.equals("doc2ext")) {
            this.doc2ext(req, resp);
        } else if (method.equals("doc2struct")) {
            this.doc2struct(req, resp);
        }
    }

    private void doc2struct(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String msg = null;
        List<File> files = new ArrayList<>();
        try {
            files = getUploadFile(req, MigrateGlobal.DOC_FILE_SIZE_LIMIT, MigrateGlobal.DOC_FILE_SIZE_LIMIT, UPLOAD_DIR);
        } catch (Exception e) {
            msg = "解析文件出现错误";
            logger.error("解析文件出现错误", e);
        }
        Response response;
        if (!files.isEmpty()) {
            response = fileService.toMarkdown(files.get(0));
            if (response == null) {
                response = Response.builder().status("failed").msg("处理文件出错").build();
            }
        } else {
            response = Response.builder().status("failed").msg(msg).build();
        }
        responsePrint(resp, toJson(response));
    }

    private void doc2ext(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String msg = null;
        List<File> files = new ArrayList<>();
        try {
            files = getUploadFile(req, MigrateGlobal.DOC_FILE_SIZE_LIMIT, MigrateGlobal.DOC_FILE_SIZE_LIMIT, UPLOAD_DIR);
        } catch (Exception e) {
            msg = "解析文件出现错误";
            logger.error("解析文件出现错误", e);
        }
        FileChunkResponse response;
        if (!files.isEmpty()) {
            response = fileService.extractContent(files.get(0));
            if (response == null) {
                response = FileChunkResponse.builder().status("failed").msg("处理文件出错").build();
            }
        } else {
            response = FileChunkResponse.builder().status("failed").msg(msg).build();
        }
        responsePrint(resp, toJson(response));
    }
}

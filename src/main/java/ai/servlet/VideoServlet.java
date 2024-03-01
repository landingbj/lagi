package ai.servlet;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FileUtils;

import ai.migrate.service.ApiService;
import ai.utils.AiGlobal;
import ai.utils.MigrateGlobal;

public class VideoServlet extends BaseServlet {
    private static final long serialVersionUID = 1L;

    private ApiService apiService = new ApiService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        String url = req.getRequestURI();
        String method = url.substring(url.lastIndexOf("/") + 1);

        if (method.equals("motInference")) {
            this.motInference(req, resp);
        } else if (method.equals("mmeditingInference")) {
            this.mmeditingInference(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doGet(req, resp);
    }

    private void motInference(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=utf-8");
        String tempPath = this.getServletContext().getRealPath(AiGlobal.DIR_TEMP);
        File tmpFile = new File(tempPath);
        if (!tmpFile.exists()) {
            tmpFile.mkdir();
        }
        String result = null;
        try {
            DiskFileItemFactory factory = new DiskFileItemFactory();
            ServletFileUpload upload = new ServletFileUpload(factory);
            upload.setHeaderEncoding("UTF-8");
            if (!ServletFileUpload.isMultipartContent(req)) {
                return;
            }
            upload.setFileSizeMax(MigrateGlobal.VIDEO_FILE_SIZE_LIMIT);
            upload.setSizeMax(MigrateGlobal.VIDEO_FILE_SIZE_LIMIT);
            List<FileItem> list = upload.parseRequest(req);

            for (FileItem item : list) {
                if (item.isFormField()) {
                    String name = item.getFieldName();
                    String value = item.getString("UTF-8");
                } else {
                    String filename = item.getName();
                    if (filename == null || filename.trim().equals("")) {
                        continue;
                    }
                    String extName = filename.substring(filename.lastIndexOf("."));
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
                    String newName = sdf.format(new Date()) + ("" + Math.random()).substring(2, 6) + extName;
                    InputStream in = item.getInputStream();
                    File file = new File(tempPath + "/" + newName);
                    FileUtils.copyInputStreamToFile(in, file);
                    result = apiService.motInference(file.getAbsolutePath(), req);
                }
            }
        } catch (FileUploadException e) {
            e.printStackTrace();
        }
        responsePrint(resp, result);
    }
    
    private void mmeditingInference(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=utf-8");
        String tempPath = this.getServletContext().getRealPath(AiGlobal.DIR_TEMP);
        File tmpFile = new File(tempPath);
        if (!tmpFile.exists()) {
            tmpFile.mkdir();
        }
        String result = null;
        try {
            DiskFileItemFactory factory = new DiskFileItemFactory();
            ServletFileUpload upload = new ServletFileUpload(factory);
            upload.setHeaderEncoding("UTF-8");
            if (!ServletFileUpload.isMultipartContent(req)) {
                return;
            }
            upload.setFileSizeMax(MigrateGlobal.VIDEO_FILE_SIZE_LIMIT);
            upload.setSizeMax(MigrateGlobal.VIDEO_FILE_SIZE_LIMIT);
            List<FileItem> list = upload.parseRequest(req);

            for (FileItem item : list) {
                if (item.isFormField()) {
                    String name = item.getFieldName();
                    String value = item.getString("UTF-8");
                } else {
                    String filename = item.getName();
                    if (filename == null || filename.trim().equals("")) {
                        continue;
                    }
                    String extName = filename.substring(filename.lastIndexOf("."));
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
                    String newName = sdf.format(new Date()) + ("" + Math.random()).substring(2, 6) + extName;
                    InputStream in = item.getInputStream();
                    File file = new File(tempPath + "/" + newName);
                    FileUtils.copyInputStreamToFile(in, file);
                    result = apiService.mmeditingInference(file.getAbsolutePath(), req);
                }
            }
        } catch (FileUploadException e) {
            e.printStackTrace();
        }
        responsePrint(resp, result);
    }
}

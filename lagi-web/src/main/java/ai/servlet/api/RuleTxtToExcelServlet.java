package ai.servlet.api;

import ai.common.pojo.Response;
import ai.dto.RuleTxtToExcelRecord;
import ai.servlet.BaseServlet;
import ai.sevice.RuleTxtToExcelService;
import com.google.gson.Gson;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class RuleTxtToExcelServlet extends BaseServlet {
    private static final String UPLOAD_DIR = "upload";
    private static final Logger logger = LoggerFactory.getLogger(RuleTxtToExcelServlet.class);
    private final Gson gson = new Gson();

    private final RuleTxtToExcelService ruleTxtToExcelService = new RuleTxtToExcelService();


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
        upload.setSizeMax(500 * 1024 * 1024);

        File txtFile = null;

        try {
            List<FileItem> items = upload.parseRequest(req);
            logger.info("Number of form items: {}", items.size());

            for (FileItem item : items) {
                if (!item.isFormField()) {
                    String fileName = item.getName();
                    if (fileName != null && !fileName.isEmpty()) {
                        fileName = fileName.replaceAll("[^a-zA-Z0-9.-]", "_");
                        fileName = System.currentTimeMillis() + "_" + fileName;
                        txtFile = new File(uploadPath.toFile(), fileName);
                        item.write(txtFile);
                        logger.info("Uploaded file saved: {} (size: {} bytes)", txtFile.getAbsolutePath(), txtFile.length());
                    }
                }
            }

            if (txtFile == null || !txtFile.exists()) {
                logger.warn("No file uploaded or file not saved");
                Response response = Response.builder().status("failed").msg("No file uploaded").build();
                responsePrint(resp, gson.toJson(response));
                return;
            }

            generateExcel(txtFile, resp);
        } catch (Exception e) {
            logger.error("Request processing failed", e);
            Response response = Response.builder().status("failed").msg("Request processing failed: " + e.getMessage()).build();
            responsePrint(resp, gson.toJson(response));
        } finally {
            if (txtFile != null && txtFile.exists()) {
                logger.info("Temporary file retained for debugging: {}", txtFile.getAbsolutePath());
            }
        }
    }


    private void generateExcel(File txtFile, HttpServletResponse resp) {
        logger.info("Generating Excel for file: {}", txtFile.getName());
        try {
            List<RuleTxtToExcelRecord> records = ruleTxtToExcelService.parseFile(txtFile.toPath());

            try (Workbook wb = ruleTxtToExcelService.generateExcel(records)) {
                // 设置响应头为 Excel 下载
                resp.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
                resp.setHeader("Content-Disposition", "attachment; filename=RuleSummary.xlsx");

                try (OutputStream out = resp.getOutputStream()) {
                    wb.write(out);
                    out.flush();
                    logger.info("Excel sent to client (rows: {})", records.size());
                }
            }
        } catch (Exception e) {
            logger.error("Excel generation failed", e);
            Response response = Response.builder().status("failed").msg("Excel generation failed: " + e.getMessage()).build();
            try {
                responsePrint(resp, gson.toJson(response));
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }
}
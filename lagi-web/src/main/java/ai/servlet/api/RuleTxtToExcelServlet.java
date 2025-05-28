package ai.servlet.api;

import ai.common.pojo.Response;
import ai.servlet.BaseServlet;
import com.google.gson.Gson;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class RuleTxtToExcelServlet extends BaseServlet {
    private static final String UPLOAD_DIR = "upload";
    private static final Logger logger = LoggerFactory.getLogger(RuleTxtToExcelServlet.class);
    private final Gson gson = new Gson();

    // 用来存储一条记录
    static class Record {
        String rule;
        String no;
        String block = "";   // 本例始终空
        String reason;
        String decision = ""; // 本例始终空
    }

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
        upload.setSizeMax(200 * 1024 * 1024); // 200MB 限制

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
            List<Record> records = parseFile(txtFile.toPath());

            try (Workbook wb = new XSSFWorkbook()) {
                Sheet sheet = wb.createSheet("规则汇总");

                // 表头
                String[] headers = {
                    "Violation Rule", "No.", "Block of Design Rule Violation", "Reason", "Decision"
                };
                Row hr = sheet.createRow(0);
                CellStyle headStyle = wb.createCellStyle();
                Font font = wb.createFont();
                font.setBold(true);
                headStyle.setFont(font);
                for (int c = 0; c < headers.length; c++) {
                    Cell cell = hr.createCell(c);
                    cell.setCellValue(headers[c]);
                    cell.setCellStyle(headStyle);
                }

                // 数据行
                int rowIdx = 1;
                for (Record r : records) {
                    Row row = sheet.createRow(rowIdx++);
                    row.createCell(0).setCellValue(r.rule);
                    row.createCell(1).setCellValue(r.no);
                    row.createCell(2).setCellValue(r.block);
                    row.createCell(3).setCellValue(r.reason);
                    row.createCell(4).setCellValue(r.decision);
                }

                // 自动列宽
                for (int c = 0; c < headers.length; c++) {
                    sheet.autoSizeColumn(c);
                }

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

    private  List<Record> parseFile(Path txt) throws IOException {
        List<String> lines = Files.readAllLines(txt, StandardCharsets.UTF_8);
        List<Record> result = new ArrayList<>();

        Pattern header = Pattern.compile("^[A-Z0-9._]+$");
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i).trim();

            // 判断是否为规则头
            if (header.matcher(line).matches()) {
                String ruleId = line;
                if (i + 1 >= lines.size()) continue;

                // 读取No.
                String[] nums = lines.get(i + 1).trim().split("\\s+");
                int no = Integer.parseInt(nums[0]);
                if (no == 0) continue;

                Record rec = new Record();
                rec.rule = ruleId;
                rec.no = String.valueOf(no);

                // 从第三行开始找“{”
                i += 2;
                StringBuilder reason = new StringBuilder();
                boolean inBlock = false;

                while (i < lines.size()) {
                    String content = lines.get(i).trim();

                    if (content.contains("{")) {
                        inBlock = true;
                        // 提取“@”这一行
                        int atIndex = content.indexOf('@');
                        if (atIndex != -1) {
                            reason.append(content.substring(atIndex + 1).trim()).append(" ");
                        }
                    } else if (inBlock && content.equals("}")) {
                        break;
                    } else if (inBlock) {
                        reason.append(content).append(" ");
                    }
                    i++;
                }
                rec.reason = reason.toString().trim();
                result.add(rec);
            }
        }
        return result;
    }

}
package ai.servlet.api;

import ai.common.pojo.Response;
import ai.dto.BlockDesc;
import ai.dto.DxDiagnosis;
import ai.dto.Rectangle;
import ai.servlet.BaseServlet;
import ai.sevice.DxImageService;
import ai.sevice.ImageBlockService;
import ai.utils.MigrateGlobal;
import com.google.gson.Gson;
import org.apache.poi.xwpf.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.*;

public class SafetyMechanismApiServlet extends BaseServlet {
    private static final String UPLOAD_DIR = "/upload";
    private static final Logger logger = LoggerFactory.getLogger(SafetyMechanismApiServlet.class);
    private final Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        logger.info("收到 POST 请求: {}", req.getRequestURI());
        req.setCharacterEncoding("UTF-8");
        resp.setHeader("Content-Type", "application/json;charset=utf-8");
        String url = req.getRequestURI();
        String method = url.substring(url.lastIndexOf("/") + 1);
        logger.debug("提取的方法: {}", method);

        if (method.equals("generateSafetyMechanismTable")) {
            this.generateSafetyMechanismTable(req, resp);
        } else {
            logger.warn("不支持的方法: {}", method);
            Response response = Response.builder().status("failed").msg("不支持的方法").build();
            responsePrint(resp, gson.toJson(response));
        }
    }

    private void generateSafetyMechanismTable(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        logger.info("开始执行 generateSafetyMechanismTable");
        String msg = null;
        List<File> files = new ArrayList<>();

        try {
            logger.debug("尝试获取上传的文件");
            files = getUploadFile(req, MigrateGlobal.DOC_FILE_SIZE_LIMIT, MigrateGlobal.DOC_FILE_SIZE_LIMIT, UPLOAD_DIR);
            logger.info("上传文件数量: {}", files.size());
        } catch (Exception e) {
            msg = "解析文件出现错误";
            logger.error("解析上传文件失败", e);
        }

        if (!files.isEmpty()) {
            try {
                // Step 1: 调用服务分析图片
                logger.info("开始分析图片文件: {}", files.get(0).getName());
                List<BlockDesc> blockDescList = callImageBlockService(files.get(0));
                List<DxDiagnosis> dxDiagnoses = callDxImageService(files.get(0));

                // Step 2: 建立坐标包含关系并合并数据
                logger.info("开始建立坐标包含关系");
                List<Map<String, String>> tableData = buildTableData(blockDescList, dxDiagnoses);

                // Step 3: 生成 Word 文件
                logger.info("开始生成 Word 文件");
                File wordFile = generateWordFile(tableData);
                logger.info("Word 文件生成成功: {}", wordFile.getAbsolutePath());
                sendFile(resp, wordFile);
                logger.info("Word 文件已发送给客户端");

            } catch (Exception e) {
                logger.error("生成安全机制表失败", e);
                resp.getWriter().write("{\"status\": \"failed\", \"msg\": \"生成安全机制表失败\"}");
            }
        } else {
            logger.warn("没有上传文件，错误信息: {}", msg);
            Response response = Response.builder().status("failed").msg(msg != null ? msg : "未上传文件").build();
            responsePrint(resp, gson.toJson(response));
        }
    }

    private List<BlockDesc> callImageBlockService(File imageFile) {
        // 调用 ImageBlockService
        logger.debug("调用 ImageBlockService 分析图片: {}", imageFile.getName());
        ImageBlockService imageBlockService = new ImageBlockService();
        String imagePath = imageFile.getAbsolutePath();
        try {
            return imageBlockService.analyzeBdImage(imagePath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private List<DxDiagnosis> callDxImageService(File imageFile) {
        // 调用 DxImageService
        logger.debug("调用 DxImageService 分析图片: {}", imageFile.getName());
        DxImageService dxImageService = new DxImageService();
        String imagePath = imageFile.getAbsolutePath();
        try {
            return dxImageService.analyzeImage(imagePath);
        } catch (Exception e) {
            logger.error("DxImageService 分析失败", e);
            throw new RuntimeException("DxImageService 分析失败");
        }
    }

    private List<Map<String, String>> buildTableData(List<BlockDesc> blockDescList, List<DxDiagnosis> dxDiagnoses) {
        logger.debug("开始构建表格数据，BlockDesc 数量: {}, DxDiagnosis 数量: {}", blockDescList.size(), dxDiagnoses.size());
        Map<String, Map<String, List<String>>> blockDataMap = new HashMap<>();

        // Step 1: 按 Block ID 聚合同一 Block 的数据
        for (DxDiagnosis dx : dxDiagnoses) {
            Rectangle dxRect = dx.getRectangle();
            for (BlockDesc block : blockDescList) {
                Rectangle blockRect = block.getRectangle();
                if (isRectangleContained(dxRect, blockRect)) {
                    String blockId = String.valueOf(block.getId());
                    Map<String, List<String>> rowData = blockDataMap.computeIfAbsent(blockId, k -> new HashMap<>());
                    rowData.computeIfAbsent("HSR_ID", k -> new ArrayList<>()).add(dx.getShortDesc());
                    if (dx.getId().startsWith("DI")) {
                        rowData.computeIfAbsent("FMEDA_DI", k -> new ArrayList<>()).add(dx.getId());
                    } else if (dx.getId().startsWith("DX")) {
                        rowData.computeIfAbsent("FMEDA_DX", k -> new ArrayList<>()).add(dx.getId());
                    }
                    logger.debug("匹配成功: DxDiagnosis ID={} 包含在 Block ID={}", dx.getId(), block.getId());
                }
            }
        }

        // Step 2: 转换为表格数据，合并同 Block ID 的内容
        List<Map<String, String>> tableData = new ArrayList<>();
        for (Map.Entry<String, Map<String, List<String>>> entry : blockDataMap.entrySet()) {
            Map<String, String> row = new HashMap<>();
            String blockId = entry.getKey();
            Map<String, List<String>> data = entry.getValue();
            row.put("Block_ID", blockId);
            row.put("HSR_ID", String.join(";", data.getOrDefault("HSR_ID", new ArrayList<>())));
            row.put("FMEDA_DI", String.join(";", data.getOrDefault("FMEDA_DI", new ArrayList<>())));
            row.put("FMEDA_DX", String.join(";", data.getOrDefault("FMEDA_DX", new ArrayList<>())));
            tableData.add(row);
        }

        // Step 3: 按 Block ID 排序
        tableData.sort(Comparator.comparing(row -> Integer.parseInt(row.get("Block_ID"))));
        logger.info("表格数据构建完成，行数: {}", tableData.size());
        return tableData;
    }

    private boolean isRectangleContained(Rectangle inner, Rectangle outer) {
        boolean isStrictlyInside = inner.getX0() >= outer.getX0()
                && inner.getY0() >= outer.getY0()
                && inner.getX1() <= outer.getX1()
                && inner.getY1() <= outer.getY1();

        double x0 = Math.max(inner.getX0(), outer.getX0());
        double y0 = Math.max(inner.getY0(), outer.getY0());
        double x1 = Math.min(inner.getX1(), outer.getX1());
        double y1 = Math.min(inner.getY1(), outer.getY1());

        double intersectionArea = (x1 > x0 && y1 > y0) ? (x1 - x0) * (y1 - y0) : 0;
        double innerArea = (inner.getX1() - inner.getX0()) * (inner.getY1() - inner.getY0());

        double ratio = intersectionArea / innerArea;

        return isStrictlyInside || ratio >= 0.5;
    }

    private File generateWordFile(List<Map<String, String>> tableData) throws IOException {
        logger.info("开始生成 Word 文件，表格行数: {}", tableData.size());
        File file = File.createTempFile("SafetyMechanism", ".docx");
        logger.debug("创建临时文件: {}", file.getAbsolutePath());

        try (FileOutputStream out = new FileOutputStream(file)) {
            XWPFDocument document = new XWPFDocument();

            // 创建表格
            XWPFTable table = document.createTable(tableData.size() + 1, 4);
            // 设置表头
            XWPFTableRow headerRow = table.getRow(0);
            headerRow.getCell(0).setText("HSR ID");
            headerRow.getCell(1).setText("Block ID");
            headerRow.getCell(2).setText("FMEDA DI(Safety Mechanism)");
            headerRow.getCell(3).setText("FMEDA DX(Safety Mechanism)");

            // 填充表格内容
            for (int i = 0; i < tableData.size(); i++) {
                Map<String, String> rowData = tableData.get(i);
                XWPFTableRow row = table.getRow(i + 1);
                row.getCell(0).setText(rowData.get("HSR_ID"));
                row.getCell(1).setText(rowData.get("Block_ID"));
                row.getCell(2).setText(rowData.get("FMEDA_DI"));
                row.getCell(3).setText(rowData.get("FMEDA_DX"));
                logger.debug("填充表格行 {}: {}", i + 1, rowData);
            }

            document.write(out);
            logger.info("Word 文件写入成功: {}", file.getAbsolutePath());
        } catch (Exception e) {
            logger.error("生成 Word 文件失败", e);
            throw e;
        }

        return file;
    }

    private void sendFile(HttpServletResponse resp, File file) throws IOException {
        logger.info("开始发送文件: {}", file.getAbsolutePath());
        resp.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        resp.setHeader("Content-Disposition", "attachment; filename=\"SafetyMechanism.docx\"");

        try (FileInputStream in = new FileInputStream(file);
             OutputStream out = resp.getOutputStream()) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
            out.flush();
            logger.info("文件发送成功");
        } finally {
            file.delete();
            logger.debug("临时文件已删除: {}", file.getAbsolutePath());
        }
    }
}
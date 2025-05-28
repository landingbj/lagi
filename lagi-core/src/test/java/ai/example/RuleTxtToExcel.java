package ai.example;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;

public class RuleTxtToExcel {

    // 用来存储一条记录
    static class Record {
        String rule;
        String no;
        String block = "";   // 本例始终空
        String reason;
        String decision = ""; // 本例始终空
    }

    public static void main(String[] args) throws Exception {
        Path txt = Paths.get("C:\\Users\\24175\\Documents\\络明芯需求\\需求250414\\MX61A_1A.drc.results.txt");     // 输入文件
        Path xlsx = Paths.get("C:\\Users\\24175\\Documents\\络明芯需求\\需求250414\\MX61A_1A.drc.results_out.xlsx");  // 输出文件

        List<Record> records = parseFile(txt);
        writeExcel(records, xlsx);
        System.out.println("生成完毕: " + xlsx.toAbsolutePath());
    }

    private static List<Record> parseFile(Path txt) throws IOException {
        List<String> lines = Files.readAllLines(txt, StandardCharsets.UTF_8);
        List<Record> result = new ArrayList<>();

        Pattern ruleHeader = Pattern.compile("^[A-Z0-9_.]+$");
        Pattern noLine = Pattern.compile("^\\d+\\s+\\d+\\s+\\d+\\s+[A-Z][a-z]{2}.*\\d{4}$");

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i).trim();

            // 判断是否为规则头
            if (ruleHeader.matcher(line).matches()) {
                String ruleId = line;

                // 下一行必须是 No.
                if (i + 1 >= lines.size()) continue;
                String noLineText = lines.get(i + 1).trim();
                if (!noLine.matcher(noLineText).matches()) continue;

                String[] parts = noLineText.split("\\s+");
                int no = Integer.parseInt(parts[0]);
                if (no == 0) continue;

                Record rec = new Record();
                rec.rule = ruleId;
                rec.no = String.valueOf(no);

                i += 2; // 移动到下一个逻辑块

                StringBuilder reason = new StringBuilder();
                if (i < lines.size() && lines.get(i).contains("{")) {
                    // 是 {} 块结构
                    boolean inBlock = false;
                    while (i < lines.size()) {
                        String content = lines.get(i).trim();
                        if (content.startsWith("p ") || content.matches("^\\d+\\s+\\d+$")) {
                            i++; continue; // 跳过 polygon 坐标数据
                        }

                        if (content.contains("{")) {
                            inBlock = true;
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
                } else {
                    // 是普通格式
                    while (i < lines.size()) {
                        String content = lines.get(i).trim();

                        // 下一条规则开始
                        if (ruleHeader.matcher(content).matches()) {
                            i--; break;
                        }

                        if (content.startsWith("p ") || content.matches("^\\d+\\s+\\d+$")) {
                            i++; continue; // 跳过坐标数据
                        }

                        reason.append(content).append(" ");
                        i++;
                    }
                }

                rec.reason = reason.toString().trim();
                result.add(rec);
            }
        }
        return result;
    }




    private static void writeExcel(List<Record> records, Path out) throws IOException {
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
            try (OutputStream outStream = Files.newOutputStream(out)) {
                wb.write(outStream);
            }
        }
    }
}

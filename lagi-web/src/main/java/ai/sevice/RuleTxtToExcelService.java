package ai.sevice;

import ai.dto.RuleTxtToExcelRecord;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RuleTxtToExcelService {
    private static final Pattern RULE_PATTERN = Pattern.compile(
            "^\\s*\\d+\\s+\\d+\\s+\\d+\\s+[A-Za-z]{3}\\s+\\d{1,2}\\s+\\d{2}:\\d{2}:\\d{2}\\s+\\d{4}\\s*$"
    );

    private static final Pattern NOT_VALID_PATTERN = Pattern.compile("^(?:([a-zA-Z])\\s+)?(-?\\d+)(?:\\s+(-?\\d+))+$");

    @Data
    @AllArgsConstructor
    public static class RuleTxt {
        private int dateLineNum;
        private List<String> lines;
    }

    public Workbook generateExcel(List<RuleTxtToExcelRecord> records) {
        Workbook wb = new XSSFWorkbook();
        Sheet sheet = wb.createSheet("规则汇总");
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
        int rowIdx = 1;
        for (RuleTxtToExcelRecord r : records) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(r.getRule());
            row.createCell(1).setCellValue(r.getNo());
            row.createCell(2).setCellValue(r.getBlock());
            row.createCell(3).setCellValue(r.getReason());
            row.createCell(4).setCellValue(r.getDecision());
        }
        for (int c = 0; c < headers.length; c++) {
            sheet.autoSizeColumn(c);
        }
        return wb;
    }

    private List<Path> getAllFilePaths(String path) throws IOException {
        Path dir = Paths.get(path);
        if (!Files.isDirectory(dir)) {
            throw new IllegalArgumentException("Provided path is not a directory: " + dir);
        }
        try (Stream<Path> paths = Files.walk(dir)) {
            return paths
                    .filter(Files::isRegularFile)
                    .collect(Collectors.toList());
        }
    }


    private boolean notValid(String input) {
        if (input == null || input.isEmpty()) {
            return true;
        }
        if (input.startsWith("Rule File Pathname") || input.startsWith("Rule File Title") || input.startsWith("<ENCRYPTED LINE")) {
            return true;
        }
        return NOT_VALID_PATTERN.matcher(input).matches();
    }

    public List<RuleTxt> splitRules(List<String> allLines, List<Integer> splitLines) {
        List<RuleTxt> result = new ArrayList<>();
        int totalLines = allLines.size();
        int currentStart = 1;
        for (int splitLine : splitLines) {
            if (splitLine > currentStart && splitLine <= totalLines) {
                List<String> partContent = allLines.subList(currentStart - 1, splitLine - 1);
                result.add(new RuleTxt(currentStart, partContent));
                currentStart = splitLine;
            }
        }
        if (currentStart <= totalLines) {
            List<String> partContent = allLines.subList(currentStart - 1, totalLines);
            result.add(new RuleTxt(currentStart, partContent));
        }
        return result;
    }

    public List<RuleTxtToExcelRecord> parseFile(Path filePath) throws IOException {
        List<RuleTxtToExcelRecord> records = new ArrayList<>();
        List<Integer> splitLineNums = new ArrayList<>();
        Map<Integer, Integer> originalLineNums = new HashMap<>();
        List<String> filteredLines = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(filePath)) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                lineNumber++;
                if (notValid(line)) {
                    continue;
                }
                filteredLines.add(line);
                if (RULE_PATTERN.matcher(line).matches()) {
                    int num = filteredLines.size() - 1;
                    originalLineNums.put(num, lineNumber);
                    splitLineNums.add(num);
                }
            }
        }
        List<RuleTxt> ruleTxtList = splitRules(filteredLines, splitLineNums);
        for (RuleTxt entry : ruleTxtList) {
            List<String> lines = entry.getLines();
            if (lines == null || lines.isEmpty()) {
                continue;
            }
            if (lines.size() < 2 || lines.get(1).trim().startsWith("0 0 ")) {
                continue;
            }
            printRule(entry);
            RuleTxtToExcelRecord record = parseRuleTxtToExcelRecord(entry);
            records.add(record);
        }

        return records;
    }

    private RuleTxtToExcelRecord parseRuleTxtToExcelRecord(RuleTxt ruleTxt) {
        List<String> lines = ruleTxt.getLines();
        String rule = lines.get(0).trim();
        String no = lines.get(1).split(" ")[0].trim();
        StringBuilder sb = new StringBuilder();
        for (int i = 2; i < lines.size(); i++) {
            String line = lines.get(i).replaceAll(rule, "")
                    .replaceAll("\\{", "")
                    .replaceAll("}", "")
                    .replaceAll("@", "")
                    .trim();
            if (line.isEmpty()) {
                continue;
            }
            sb.append(line).append("\n");
        }
        String reason = sb.toString();
        RuleTxtToExcelRecord record = new RuleTxtToExcelRecord();
        record.setRule(rule);
        record.setNo(no);
        record.setReason(reason);
        printRuleRecord(record);
        return record;
    }

    private void printRuleRecord(RuleTxtToExcelRecord record) {
        System.out.println(record.getRule());
        System.out.println(record.getNo());
        System.out.println(record.getReason());
    }

    private void printRule(RuleTxt ruleTxt) {
        for (String line : ruleTxt.getLines()) {
            System.out.println(line);
        }
    }

    private String writeWorkbookToFile(Workbook workbook, Path outputPath) throws IOException {
        try (FileOutputStream fileOut = new FileOutputStream(outputPath.toFile())) {
            workbook.write(fileOut);
        }
        return outputPath.toString();
    }

    public static void main(String[] args) throws IOException {
        RuleTxtToExcelService ruleTxtToExcelService = new RuleTxtToExcelService();

        List<Path> txtFiles = ruleTxtToExcelService.getAllFilePaths("E:/Desktop/络明芯规则");

        for (Path txtFile : txtFiles) {
            if (!txtFile.toString().endsWith(".txt")) {
                continue;
            }
//            if (!txtFile.toString().endsWith("MX61A_1A.drc.results.txt")) {
//                continue;
//            }
            System.out.println(txtFile);
            List<RuleTxtToExcelRecord> records = ruleTxtToExcelService.parseFile(txtFile);

            for (RuleTxtToExcelRecord record : records) {
                System.out.println(record);
            }
            String outputFileName = txtFile.getFileName().toString().replace(".txt", ".xlsx");
            Path outputPath = Paths.get(txtFile.getParent().toString(), outputFileName);
            try (Workbook workbook = ruleTxtToExcelService.generateExcel(records)) {
                String outputFilePath = ruleTxtToExcelService.writeWorkbookToFile(workbook, outputPath);
                System.out.println("Excel file created: " + outputFilePath);
            }
        }
    }
}

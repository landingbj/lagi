package ai.utils;

import ai.common.pojo.FileChunkResponse;
import ai.vector.pojo.ExcelPage;
import cn.hutool.json.JSONObject;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.junit.Test;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;

/**
 * @Author tsg
 * @Date 2025/1/1 13:44
 */
public class EasyExcelUtil {

    //用于标记是否为合并单元格
    private static Map<Integer, Boolean> mergedCellFlags = new HashMap<>();
    //用于记录上一行内容
    private static Map<Integer, String> lastRowData = new HashMap<>();

    public static class Page<T> {
        private int pageIndex;
        private int pageSize;
        private List<T> items;

        public Page(int pageIndex, int pageSize, List<T> items) {
            this.pageIndex = pageIndex;
            this.pageSize = pageSize;
            this.items = items;
        }

        public int getPageIndex() {
            return pageIndex;
        }

        public int getPageSize() {
            return pageSize;
        }

        public List<T> getItems() {
            return items;
        }
    }

    public static Map<String, List<List<Object>>> readExcel(File file) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(file);
        List<List<Object>> data = new ArrayList<>();
        List<Object> header = new ArrayList<>();
        EasyExcel.read(fileInputStream)
                .sheet()
                .headRowNumber(0)
                .registerReadListener(new ReadListener<Map<Integer, String>>() {
                    @Override
                    public void invoke(Map<Integer, String> row, AnalysisContext context) {
                        if (context.readRowHolder().getRowIndex() == 0) {
                            header.addAll(row.values());
                        } else {
                            List<Object> rowData = new ArrayList<>();
                            for (Map.Entry<Integer, String> entry : row.entrySet()) {
                                Integer columnIndex = entry.getKey();
                                String cellValue = entry.getValue();
                                String lastRowValue = lastRowData.get(columnIndex);
                                if (cellValue == null || cellValue.isEmpty()) {
                                    if (mergedCellFlags.containsKey(columnIndex) && mergedCellFlags.get(columnIndex)) {
                                        rowData.add(lastRowValue + "\t");
                                    } else {
                                        rowData.add("");
                                    }
                                    row.put(columnIndex,lastRowValue);
                                } else {
                                    rowData.add(cellValue + "\t");
                                }
                                if (cellValue != null && !cellValue.isEmpty() && !mergedCellFlags.containsKey(columnIndex)) {
                                    mergedCellFlags.put(columnIndex, true);
                                } else {
                                    mergedCellFlags.put(columnIndex, false);
                                }
                            }
                            data.add(rowData);
                        }
                        System.out.println(row);
                        System.out.println("-------------------------------------------------");
                        lastRowData = new HashMap<>(row);
                    }
                    @Override
                    public void doAfterAllAnalysed(AnalysisContext context) {
                        // Once the processing is complete
                    }
                })
                .doRead();

        fileInputStream.close();
        Map<String, List<List<Object>>> result = new HashMap<>();
        result.put("header", Collections.singletonList(header));
        result.put("data", data);
        return result;
    }
    public static String getFileEncode(String path) {
        String charset ="asci";
        byte[] first3Bytes = new byte[3];
        BufferedInputStream bis = null;
        try {
            boolean checked = false;
            bis = new BufferedInputStream(new FileInputStream(path));
            bis.mark(0);
            int read = bis.read(first3Bytes, 0, 3);
            if (read == -1)
                return charset;
            if (first3Bytes[0] == (byte) 0xFF && first3Bytes[1] == (byte) 0xFE) {
                charset = "Unicode";//UTF-16LE
                checked = true;
            } else if (first3Bytes[0] == (byte) 0xFE && first3Bytes[1] == (byte) 0xFF) {
                charset = "Unicode";//UTF-16BE
                checked = true;
            } else if (first3Bytes[0] == (byte) 0xEF && first3Bytes[1] == (byte) 0xBB && first3Bytes[2] == (byte) 0xBF) {
                charset = "UTF8";
                checked = true;
            }
            bis.reset();
            if (!checked) {
                int len = 0;
                int loc = 0;
                while ((read = bis.read()) != -1) {
                    loc++;
                    if (read >= 0xF0)
                        break;
                    if (0x80 <= read && read <= 0xBF) //单独出现BF以下的，也算是GBK
                        break;
                    if (0xC0 <= read && read <= 0xDF) {
                        read = bis.read();
                        if (0x80 <= read && read <= 0xBF)
                            //双字节 (0xC0 - 0xDF) (0x80 - 0xBF),也可能在GB编码内
                            continue;
                        else
                            break;
                    } else if (0xE0 <= read && read <= 0xEF) {
                        read = bis.read();
                        if (0x80 <= read && read <= 0xBF) {
                            read = bis.read();
                            if (0x80 <= read && read <= 0xBF) {
                                charset = "UTF-8";
                                break;
                            } else
                                break;
                        } else
                            break;
                    }
                }
                //TextLogger.getLogger().info(loc + " " + Integer.toHexString(read));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (bis != null) {
                try {
                    bis.close();
                } catch (IOException ex) {
                }
            }
        }
        return charset;
    }

    public static Map<String, List<List<Object>>> readCsv(String filePath) throws IOException {
        List<List<Object>> data = new ArrayList<>();
        List<Object> header = new ArrayList<>();

        List<Charset> possibleCharsets = Arrays.asList(Charset.forName("GBK"), Charset.forName("ISO-8859-1"));

        for (Charset charset : possibleCharsets) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), charset))) {
                String line;
                boolean isHeader = true;

                while ((line = br.readLine()) != null) {
                    String[] cells = line.split(",");

                    if (isHeader) {
                        Collections.addAll(header, cells);
                        isHeader = false;
                    } else {
                        List<Object> rowData = new ArrayList<>();
                        for (String cell : cells) {
                            rowData.add(cell + "\t");
                        }
                        data.add(rowData);
                    }
                }
                break;
            } catch (IOException e) {
                System.err.println("尝试使用编码 " + charset.name() + " 失败，尝试下一个编码...");
            }
        }
        Map<String, List<List<Object>>> result = new HashMap<>();
        result.put("header", Collections.singletonList(header));
        result.put("data", data);
        return result;
    }

    public static <T> List<Page<T>> paginate1(List<List<JSONObject>> data, int pageSize) {
        List<Page<T>> pages = new ArrayList<>();
        int currentPageIndex = 1;
        List<Object> currentPageItems = new ArrayList<>();
        StringBuilder currentPageContent = new StringBuilder();

        List<String> msgList = new ArrayList<>();
        // 处理数据行
        for (int i = 1; i < data.size(); i++) {
            List<JSONObject> dataRow = data.get(i);
            List<String> rowData = new ArrayList<>();
            String text = "";
            for (int j = 0; j < dataRow.size(); j++) {
                JSONObject cell = dataRow.get(j);
                String cellValue = cell.getStr("cellValue");
                rowData.add(cellValue);
//                if (cell.containsKey("isMerge") && cell.getBool("isMerge")) {
//                    int firstRow = cell.getInt("firstRow");
//                    int firstCol = cell.getInt("firstColumn");
//                    if (firstRow == i && firstCol == j) {
//                        rowData.add(cellValue);
//                    } else {
//                        rowData.add("");
//                    }
//                } else {
//                    rowData.add(cellValue);
//                }
            }
            text+="| " + String.join(" | ", rowData) + " |</br>";
            msgList.add(text);
        }
        for (String row : msgList) {
            StringBuilder rowContent = new StringBuilder();
            rowContent.append(row);
            while (rowContent.length() > pageSize) {
                String part = rowContent.substring(0, pageSize);
                rowContent.delete(0, pageSize);

                if (currentPageContent.length() + part.length() > pageSize) {
                    pages.add((Page<T>) new Page<>(currentPageIndex, currentPageContent.length(), new ArrayList<>(currentPageItems)));
                    currentPageIndex++;
                    currentPageItems.clear();
                    currentPageContent.setLength(0);
                }

                currentPageContent.append(part);
                currentPageItems.add(part);
            }

            if (rowContent.length() > 0) {
                if (currentPageContent.length() + rowContent.length() > pageSize) {
                    pages.add((Page<T>) new Page<>(currentPageIndex, currentPageContent.length(), new ArrayList<>(currentPageItems)));
                    currentPageIndex++;
                    currentPageItems.clear();
                    currentPageContent.setLength(0);
                }

                currentPageContent.append(rowContent);
                currentPageItems.add(rowContent.toString());
            }
        }

        if (!currentPageItems.isEmpty()) {
            pages.add((Page<T>) new Page<>(currentPageIndex, currentPageContent.length() , new ArrayList<>(currentPageItems)));
        }
        return pages;
    }
    public static <T> List<Page<T>> paginate(List<List<Object>> data, int pageSize) {
        List<Page<T>> pages = new ArrayList<>();
        int currentPageIndex = 1;
        List<Object> currentPageItems = new ArrayList<>();
        StringBuilder currentPageContent = new StringBuilder();

        for (List<Object> row : data) {
            StringBuilder rowContent = new StringBuilder();
            for (Object cell : row) {
                rowContent.append(cell.toString());
            }

            while (rowContent.length() > pageSize) {
                String part = rowContent.substring(0, pageSize);
                rowContent.delete(0, pageSize);

                if (currentPageContent.length() + part.length() > pageSize) {
                    pages.add((Page<T>) new Page<>(currentPageIndex, currentPageContent.length(), new ArrayList<>(currentPageItems)));
                    currentPageIndex++;
                    currentPageItems.clear();
                    currentPageContent.setLength(0);
                }

                currentPageContent.append(part);
                currentPageItems.add(part);
            }

            if (rowContent.length() > 0) {
                if (currentPageContent.length() + rowContent.length() > pageSize) {
                    pages.add((Page<T>) new Page<>(currentPageIndex, currentPageContent.length(), new ArrayList<>(currentPageItems)));
                    currentPageIndex++;
                    currentPageItems.clear();
                    currentPageContent.setLength(0);
                }

                currentPageContent.append(rowContent);
                currentPageItems.add(rowContent.toString());
            }
        }

        if (!currentPageItems.isEmpty()) {
            pages.add((Page<T>) new Page<>(currentPageIndex, currentPageContent.length() , new ArrayList<>(currentPageItems)));
        }
        return pages;
    }

    public static <T>  List<FileChunkResponse.Document> mergePages1(List<Page<T>> pages, String sheet,String header) {
        List<FileChunkResponse.Document> result = new ArrayList<>();
        for (Page<T> page : pages) {
            String text = "sheet工作表名："+sheet+"/n";
            text +=header;
            for (T item : page.getItems()) {
                text+=item;
            }
            FileChunkResponse.Document doc = new FileChunkResponse.Document();
            doc.setText(text);
            result.add(doc);
        }
        return result;
    }

    public static <T> List<FileChunkResponse.Document> mergePages(List<Page<T>> pages, List<Object> header) {
        List<FileChunkResponse.Document> result = new ArrayList<>();
        for (Page<T> page : pages) {
            String text = "";
            List<Object> header1 = (List<Object>) header.get(0);
            text = "表头: /n " + header1.get(0)+"/n";
            for (T item : page.getItems()) {
                text+=item+"/n";
            }
            FileChunkResponse.Document doc = new FileChunkResponse.Document();
            doc.setText(text);
            result.add(doc);

        }
        return result;
    }

    /**
     * EasyExcel获取excel的分页数据
     * @param file
     * @return
     * @throws IOException
     */
    public static List<FileChunkResponse.Document> getChunkDocumentXls(File file) throws IOException {
        Map<String, List<List<Object>>> result = readExcel(file);
        List<Object> header = Collections.singletonList(result.get("header"));
        List<List<Object>> rowData = result.get("data");
        int pageSize = 512;
        List<Page<List<Object>>> pages = paginate(rowData, pageSize);
        return mergePages(pages, header);
    }

    /**
     * Apache获取excel的分页数据
     * @param file
     * @return
     * @throws IOException
     */
    public static List<FileChunkResponse.Document> getChunkDocumentExcel(File file,Integer pageSize) throws IOException {
       List<FileChunkResponse.Document> resultDocument = new ArrayList<>();
        try {
            ExcelReader reader = ExcelUtil.getReader(file);
            List<String> sheetNames = reader.getSheetNames();
            for (int i = 0; i < sheetNames.size(); i++) {
                ExcelPage excelPage =readMergeExcel(file.getPath(),i,0,0);
                List<Integer> headerIndex = excelPage.getHeaderIndex();
                List<List<JSONObject>> result = excelPage.getData();

                if (result.size() <= 0){
                    break;
                }
                if (headerIndex.size() >= 2){
                   //表中表
                    if (headerIndex.get(0) > 1) {
                        List<JSONObject> headerRow = result.get(headerIndex.get(0));
                        StringBuilder separator = new StringBuilder();
                        String title = headerRow.get(0).getStr("cellValue");
                        separator.append(title+"/n");
                        List<String> headers = new ArrayList<>();
                        for (JSONObject cell : result.get(headerIndex.get(0)+1)) {
                            headers.add(cell.getStr("cellValue"));
                        }
                        separator.append("| " + String.join(" | ", headers) + " |</br>");
                        for (int j = 0; j < headers.size(); j++) {
                            separator.append("| --- ");
                        }
                        separator.append("|</br>");
                        List<List<JSONObject>> lastSubList = result.subList(1, headerIndex.get(0)-1);
                        List<Page<List<Object>>> pages = paginate1(lastSubList, pageSize);
                        resultDocument.addAll(mergePages1(pages, sheetNames.get(i), separator.toString()));
                    }
                    for (int msgindex = 0; msgindex < headerIndex.size()-1; msgindex++) {
                        Integer start = headerIndex.get(msgindex);
                        Integer end = headerIndex.get(msgindex+1);
                        StringBuilder separator = new StringBuilder();
                        List<JSONObject> headerRow = result.get(headerIndex.get(msgindex));
                        String title = headerRow.get(0).getStr("cellValue");
                        separator.append(title+"/n");
                        List<String> headers = new ArrayList<>();
                        for (JSONObject cell : result.get(headerIndex.get(msgindex)+1)) {
                            headers.add(cell.getStr("cellValue"));
                        }
                        separator.append("| " + String.join(" | ", headers) + " |</br>");
                        for (int j = 0; j < headers.size(); j++) {
                            separator.append("| --- ");
                        }
                        separator.append("|</br>");
                        List<List<JSONObject>> lastSubList = result.subList(start+1, end-1);
                        List<Page<List<Object>>> pages = paginate1(lastSubList, pageSize);
                        resultDocument.addAll(mergePages1(pages, sheetNames.get(i), separator.toString()));
                    }
                    if (headerIndex.get(headerIndex.size() - 1)+1 < result.size()) {
                        StringBuilder separator = new StringBuilder();
                        List<JSONObject> headerRow = result.get(headerIndex.get(headerIndex.size() - 1));
                        String title = headerRow.get(0).getStr("cellValue");
                        separator.append(title+"/n");
                        List<String> headers = new ArrayList<>();
                        for (JSONObject cell : result.get(headerIndex.get(headerIndex.size() - 1)+1)) {
                            headers.add(cell.getStr("cellValue"));
                        }
                        separator.append("| " + String.join(" | ", headers) + " |</br>");
                        for (int j = 0; j < headers.size(); j++) {
                            separator.append("| --- ");
                        }
                        separator.append("|</br>");

                        List<List<JSONObject>> lastSubList = result.subList(headerIndex.get(headerIndex.size() - 1)+1, result.size()-1);
                        List<Page<List<Object>>> pages = paginate1(lastSubList, pageSize);
                        resultDocument.addAll(mergePages1(pages, sheetNames.get(i), separator.toString()));
                    }else if (headerIndex.get(headerIndex.size() - 1)+1 == result.size()){
                        StringBuilder separator = new StringBuilder();
                        List<List<JSONObject>> lastSubList = result.subList(headerIndex.get(headerIndex.size() - 1), result.size());
                        List<Page<List<Object>>> pages = paginate1(lastSubList, pageSize);
                        resultDocument.addAll(mergePages1(pages, sheetNames.get(i), separator.toString()));
                    }

                } else {
                StringBuilder separator = new StringBuilder();
                // 获取表头
                List<JSONObject> headerRow = result.get(0);
                List<String> headers = new ArrayList<>();
                for (JSONObject cell : headerRow) {
                    headers.add(cell.getStr("cellValue"));
                }
                separator.append("| " + String.join(" | ", headers) + " |</br>");
                for (int j = 0; j < headers.size(); j++) {
                    separator.append("| --- ");
                }
                separator.append("|</br>");
                List<Page<List<Object>>> pages = paginate1(result, pageSize);
                resultDocument.addAll(mergePages1(pages, sheetNames.get(i), separator.toString()));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultDocument;
    }

    public static List<FileChunkResponse.Document> getChunkDocumentCsv(File file, Integer pageSize) throws IOException {
        Map<String, List<List<Object>>> result = readCsv(file.getPath());
        List<Object> header = Collections.singletonList(result.get("header"));
        List<List<Object>> rowData = result.get("data");
        List<Page<List<Object>>> pages = paginate(rowData, pageSize);
        return mergePages(pages, header);
    }
    public static String getExcelContent(File file) throws IOException {
        StringBuilder result = new StringBuilder();
        FileInputStream fileInputStream = new FileInputStream(file);
        List<List<Object>> data = new ArrayList<>();
        List<Object> header = new ArrayList<>();
        EasyExcel.read(fileInputStream)
                .sheet()
                .headRowNumber(0)
                .registerReadListener(new ReadListener<Map<Integer, String>>() {
                    @Override
                    public void invoke(Map<Integer, String> row, AnalysisContext context) {
                        result.append(String.join("\t", row.values()));
                        result.append("\n");
                    }
                    @Override
                    public void doAfterAllAnalysed(AnalysisContext context) {
                        // Once the processing is complete
                    }
                })
                .doRead();

        fileInputStream.close();
        return result.toString();
    }

    /**
     * 读取excel数据 支持xls格式--apache
     * @param sheetIndex    sheet页下标：从0开始
     * @param startReadLine 开始读取的行:从0开始
     * @param tailLine      去除最后读取的行
     */
    public static ExcelPage readMergeExcel(String path, int sheetIndex, int startReadLine, int tailLine) {
        List<List<JSONObject>> results = new ArrayList<>();
        Workbook wb = null;
        List<Integer> headerIndex = new ArrayList<>();
        try {
            wb = WorkbookFactory.create(new File(path));
            FormulaEvaluator formulaEvaluator = wb.getCreationHelper().createFormulaEvaluator();
            Sheet sheet = wb.getSheetAt(sheetIndex);
            headerIndex = getHeaderIndex(sheet);
            Row row = null;
            for (int i = startReadLine; i < sheet.getLastRowNum() - tailLine + 1; i++) {
                row = sheet.getRow(i);
                if (row == null)
                    continue;
                List<JSONObject> result = new ArrayList<>();
                for (Cell c : row) {
                    JSONObject values = new JSONObject();
                    boolean isMerge = isMergedRegion(sheet, i, c.getColumnIndex());
                    values.put("isMerge",isMerge);
                    // 判断是否具有合并单元格
                    if (isMerge) {
                        JSONObject rs = getMergedRegionJsonValue(sheet, row.getRowNum(), c.getColumnIndex());
                        values.putAll(rs);
                    } else {
                        values.put("cellValue",getCellValue(c,formulaEvaluator));
                    }
                    result.add(values);
                }
                results.add(result);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        ExcelPage excelPage = new ExcelPage();
        excelPage.setData(results);
        excelPage.setHeaderIndex(headerIndex);
        return excelPage;
    }
    private static List<Integer> getHeaderIndex(Sheet sheet) {
        List<Integer> headerIndex = new ArrayList<>();
        for (int rowIndex = 0; rowIndex < sheet.getPhysicalNumberOfRows(); rowIndex++) {
            Row row = sheet.getRow(rowIndex);
            if (row != null) {
                if (isHeader(sheet, rowIndex, row)) {
                    headerIndex.add(rowIndex);
                }
            }
        }
        return headerIndex;
    }
    /**
     * 检查指定行是否为表头
     *
     * @param sheet    工作表
     * @param rowIndex 行索引
     * @param row      行对象
     * @return 如果指定行为表头，返回true
     */
    private static boolean isHeader(Sheet sheet, int rowIndex, Row row) {
        int mergedCount = 0;
        boolean isMergedCellWithData = false;
        int dataCellCount = 0;
        int totalCellCount = 0;

        for (int i = 0; i < sheet.getNumMergedRegions(); i++) {
            CellRangeAddress mergedRegion = sheet.getMergedRegion(i);

            if (mergedRegion.getFirstRow() <= rowIndex && rowIndex <= mergedRegion.getLastRow()) {
                mergedCount++;
                if (mergedRegion.getFirstRow() == rowIndex) {
                    Cell cell = row.getCell(mergedRegion.getFirstColumn());
                    if (cell != null && !cell.toString().isEmpty()) {
                        isMergedCellWithData = true;
                        dataCellCount++;
                    }
                }
            }
        }
        for (int i = 0; i <= row.getLastCellNum(); i++) {
            Cell cell = row.getCell(i);
            if (cell != null && !cell.toString().isEmpty()) {
                totalCellCount++;
            }
        }
        return mergedCount == 1 && isMergedCellWithData && dataCellCount == 1 && totalCellCount == 1;
    }

    public static JSONObject getMergedRegionJsonValue(Sheet sheet, int row, int column) {
        JSONObject values = new JSONObject();
        int sheetMergeCount = sheet.getNumMergedRegions();
        for (int i = 0; i < sheetMergeCount; i++) {
            CellRangeAddress ca = sheet.getMergedRegion(i);
            int firstColumn = ca.getFirstColumn();
            int lastColumn = ca.getLastColumn();
            int firstRow = ca.getFirstRow();
            int lastRow = ca.getLastRow();
            if (row >= firstRow && row <= lastRow) {
                if (column >= firstColumn && column <= lastColumn) {
                    Row fRow = sheet.getRow(firstRow);
                    Cell fCell = fRow.getCell(firstColumn);
                    values.put("cellValue",getCellValue(fCell));
                    values.put("firstColumn",firstColumn);
                    values.put("lastColumn",lastColumn);
                    values.put("firstRow",firstRow);
                    values.put("lastRow",lastRow);
                    return values;
                }
            }
        }
        return values;
    }

    /**
     * 获取单元格的值
     */
    private static String getCellValue(Cell cell) {
        if (cell == null) {return "";}
        if (cell.getCellType() == CellType.STRING) {
            return cell.getStringCellValue();
        }
        if (cell.getCellType() == CellType.BOOLEAN) {
            return String.valueOf(cell.getBooleanCellValue());
        }
        if (cell.getCellType() ==  CellType.FORMULA) {
            return cell.getCellFormula();
        }
        if (cell.getCellType() ==  CellType.NUMERIC) {
            return String.valueOf(cell.getNumericCellValue());
        }
        return "";
    }
    private static String getCellValue(Cell cell, FormulaEvaluator formulaEvaluator) {
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                return String.valueOf(cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                switch (formulaEvaluator.evaluateInCell(cell).getCellType()) {
                    case NUMERIC:
                        return String.valueOf(cell.getNumericCellValue());
                    case STRING:
                        return cell.getStringCellValue();
                    case BOOLEAN:
                        return String.valueOf(cell.getBooleanCellValue());
                    default:
                        return "";
                }
            default:
                return "";
        }
    }

    /**
     * 判断指定的单元格是否是合并单元格
     * @param row    行下标
     * @param column 列下标
     */
    private static boolean isMergedRegion(Sheet sheet, int row, int column) {
        int sheetMergeCount = sheet.getNumMergedRegions();
        for (int i = 0; i < sheetMergeCount; i++) {
            CellRangeAddress range = sheet.getMergedRegion(i);
            int firstColumn = range.getFirstColumn();
            int lastColumn = range.getLastColumn();
            int firstRow = range.getFirstRow();
            int lastRow = range.getLastRow();
            if (row >= firstRow && row <= lastRow) {
                if (column >= firstColumn && column <= lastColumn) {
                    return true;
                }
            }
        }
        return false;
    }

    public static String getCsvContent(File file) throws IOException {
        StringBuilder result = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file.getPath()), Charset.forName("GBK")))) {
            String line;
            while ((line = br.readLine()) != null) {
                result.append(line);
                result.append("\n");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            return result.toString();
        }
    }

    /**
     * 方案1
     * @param args
     */
    public static void main1(String[] args) {
        try {
            String filePath = "C:\\Users\\ruiqing.luo\\Desktop\\rag调优\\节点分类-测试用.csv";

            File file = new File(filePath);
            Map<String, List<List<Object>>> result = readExcel(file);
//            Map<String, List<List<Object>>> result = readCsv(filePath);
            List<Object> header = Collections.singletonList(result.get("header"));
            List<List<Object>> rowData = result.get("data");
            int pageSize = 512;

            List<Page<List<Object>>> pages = paginate(rowData, pageSize);
            for (FileChunkResponse.Document mergePage : mergePages(pages, header)) {
                System.out.println(mergePage);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 方案2
     * @param args
     */
    public static void main(String[] args) {
        try {
//            String filePath = "C:\\Users\\ruiqing.luo\\Desktop\\所有表格\\poc计划表1.0v.xlsx";
            String filePath = "C:\\Users\\ruiqing.luo\\Desktop\\所有表格\\poc计划表.xlsx";
            File file = new File(filePath);
            ExcelReader reader = ExcelUtil.getReader(file);
            List<String> sheetNames = reader.getSheetNames();
            for (int i = 0; i < sheetNames.size(); i++) {
//                System.out.println(sheetNames.get(i));
                ExcelPage excelPage = readMergeExcel(filePath,i,0,0);
                List<List<JSONObject>> result = excelPage.getData();

                StringBuilder separator = new StringBuilder();
                // 获取表头
                List<JSONObject> headerRow = result.get(0);
                List<String> headers = new ArrayList<>();
                for (JSONObject cell : headerRow) {
                    headers.add(cell.getStr("cellValue"));
                }
                separator.append("| " + String.join(" | ", headers) + " |</br>");
                for (int j = 0; j < headers.size(); j++) {
                    separator.append("| --- ");
                }
                separator.append("|</br>");
                int pageSize = 512;
                List<Page<List<Object>>> pages = paginate1(result, pageSize);
                for (FileChunkResponse.Document mergePage : mergePages1(pages, sheetNames.get(i), separator.toString())) {

                    String markdownContent = mergePage.getText().replaceAll("\\| --- \\| --- \\| --- \\| --- \\| --- \\| --- \\| --- \\| --- \\| --- \\|", "\n| --- | --- | --- | --- | --- | --- | --- | --- | --- |\n");
                    System.out.println(markdownContent);
                    System.out.println();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void vv() throws IOException {
        String filePath = "C:\\Users\\ruiqing.luo\\Desktop\\rag调优\\政务数据\\各厅布局（A3版）2023.xlsx";

        File file = new File(filePath);
        Integer pageSize = 512;
        List<FileChunkResponse.Document> chunkDocumentExcel = getChunkDocumentExcel(file, pageSize);
        for (FileChunkResponse.Document document : chunkDocumentExcel) {
            System.out.println(document);
        }

    }

}
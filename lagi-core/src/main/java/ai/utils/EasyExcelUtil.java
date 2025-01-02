package ai.utils;

import ai.common.pojo.FileChunkResponse;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import org.junit.Test;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;

/**
 * @Author tsg
 * @Date 2025/1/1 13:44
 */
public class EasyExcelUtil {

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
                                rowData.add(entry.getValue()+"\t");
                            }
                            data.add(rowData);
                        }
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

    public static <T> List<FileChunkResponse.Document> mergePages(List<Page<T>> pages, List<Object> header) {
        List<FileChunkResponse.Document> result = new ArrayList<>();
        for (Page<T> page : pages) {
            String text = "";
//            System.out.println("Page " + page.getPageIndex() + " (Size: " + page.getPageSize() + ")");
            List<Object> header1 = (List<Object>) header.get(0);
            // 打印表头
//            System.out.println("表头: " + header1.get(0));
            text = "表头: " + header1.get(0)+"/n";
            for (T item : page.getItems()) {
                text+=item+"/n";
//                System.out.println("["+item+"]");
            }
//            System.out.println("-----");
            FileChunkResponse.Document doc = new FileChunkResponse.Document();
            doc.setText(text);
            result.add(doc);

        }
        return result;
    }
    public static List<FileChunkResponse.Document> getChunkDocumentXls(File file) throws IOException {
        Map<String, List<List<Object>>> result = readExcel(file);
        List<Object> header = Collections.singletonList(result.get("header"));
        List<List<Object>> rowData = result.get("data");
        int pageSize = 512;
        List<Page<List<Object>>> pages = paginate(rowData, pageSize);
        return mergePages(pages, header);
    }
    public static List<FileChunkResponse.Document> getChunkDocumentCsv(File file) throws IOException {
        Map<String, List<List<Object>>> result = readCsv(file.getPath());
        List<Object> header = Collections.singletonList(result.get("header"));
        List<List<Object>> rowData = result.get("data");
        int pageSize = 512;
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
@Test
    public void t() throws IOException {
    String f = getExcelContent(new File("C:\\Users\\ruiqing.luo\\Desktop\\当前适配智能体表列表.xls"));
    System.out.println(f);
    }


    public static void main(String[] args) {
        try {
//            String filePath = "C:\\Users\\25129\\Desktop\\公司会议纪要1202.csv";
//            Map<String, List<List<Object>>> result = readCsv(filePath);
            String filePath = "C:\\Users\\ruiqing.luo\\Desktop\\当前适配智能体表列表.xls";
//            String filePath = "C:\\Users\\25129\\Desktop\\公司会议纪要1202.xlsx";
            File file = new File(filePath);
            Map<String, List<List<Object>>> result = readExcel(file);
            List<Object> header = Collections.singletonList(result.get("header"));
            List<List<Object>> rowData = result.get("data");
            int pageSize = 512;

            List<Page<List<Object>>> pages = paginate(rowData, pageSize);
            mergePages(pages, header);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
package ai.utils;

import ai.common.pojo.FileChunkResponse;
import ai.vector.FileService;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OrdinanceExtractorUtil {
    // 正则表达式，匹配纯条列类模式（不带章节）
    private static final String ORDINANCE_FORMAT_PATTERN = "(?<=\\s|^)第[一二三四五六七八九十百千万零0-9]+条(?=\\s|$)";
    private static final FileService fileService = new FileService();

    public static boolean isOrdinanceDocument(String documentContent) {
        Pattern pattern = Pattern.compile(ORDINANCE_FORMAT_PATTERN, Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(documentContent);
        return matcher.find();
    }
    public static List<String> sliceByChapter(String documentContent, Integer maxLength) {
        List<String> result = new ArrayList<>();
        Pattern pattern = Pattern.compile("(第[一二三四五六七八九十百千万零0-9]+条)(.*?)(?=(第[一二三四五六七八九十百千万零0-9]+条)|$)", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(documentContent);
        int lastEnd = 0;
        String titile = "";
        while (matcher.find()) {
            if (matcher.start() > lastEnd) {
                if (matcher.start() < 100){
                    titile = documentContent.substring(lastEnd, matcher.start()).replaceAll("\\s+", " ").trim();
                }else {
                    String msg = documentContent.substring(lastEnd, matcher.start()).trim();
                    int start = 0;
                    while (start < msg.length()) {
                        int end = Math.min(start + maxLength, msg.length());
                        int lastSentenceEnd = Math.max(msg.lastIndexOf('.', end), msg.lastIndexOf('\n', end));
                        if (lastSentenceEnd != -1 && lastSentenceEnd > start) {
                            end = lastSentenceEnd + 1;
                        }
                        String text = msg.substring(start, end).replaceAll("\\s+", " ").trim();
                        result.add(text);
                        start = end;
                    }
                }
            }
            // 添加“第X条”及其内容
            String msg = matcher.group().replaceAll("\\s+", " ").trim();
            int start = 0;
            while (start < msg.length()) {
                int end = Math.min(start + maxLength, msg.length());
                int lastSentenceEnd = Math.max(msg.lastIndexOf('.', end), msg.lastIndexOf('\n', end));
                if (lastSentenceEnd != -1 && lastSentenceEnd > start) {
                    end = lastSentenceEnd + 1;
                }
                String text = titile+"/n"+msg.substring(start, end).replaceAll("\\s+", " ").trim();
                result.add(text);
                start = end;
            }
            lastEnd = matcher.end();
        }

        if (lastEnd+3 < documentContent.length()) {
            String msg = documentContent.substring(lastEnd).replaceAll("\\s+", " ").trim();
            int start = 0;
            while (start < msg.length()) {
                int end = Math.min(start + maxLength, msg.length());
                int lastSentenceEnd = Math.max(msg.lastIndexOf('.', end), msg.lastIndexOf('\n', end));
                if (lastSentenceEnd != -1 && lastSentenceEnd > start) {
                    end = lastSentenceEnd + 1;
                }
                String text = titile+"/n"+msg.substring(start, end).replaceAll("\\s+", " ").trim();
                result.add(text);
                start = end;
            }
        }
        return result;
    }

    public static List<FileChunkResponse.Document> getChunkDocument(String content, Integer chunkSize) {
        List<FileChunkResponse.Document> result = new ArrayList<>();
        List<String> slices = sliceByChapter(content, chunkSize);
        for (String slice : slices) {
            FileChunkResponse.Document doc = new FileChunkResponse.Document();
            doc.setText(slice);
            result.add(doc);
        }
        return result;
    }

    @Test
    public void vvv() throws IOException {
        String filePath = "C:\\Users\\ruiqing.luo\\Desktop\\rag调优\\材料\\北京地铁\\提供的资料-脱敏\\分公司年度安全考试管理办法\\《公司年度安全考试管理办法》的通知.pdf";
        String documentContent = fileService.getFileContent(new File(filePath));
        boolean isChapterDocument = isOrdinanceDocument(documentContent);
        System.out.println("是否是条例类文档" + isChapterDocument);
        List<FileChunkResponse.Document> slices = getChunkDocument(documentContent, 512);
        for (FileChunkResponse.Document slice : slices) {
            System.out.println(slice);
            System.out.println();
        }
    }
}

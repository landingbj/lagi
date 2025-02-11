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

public class ChapterExtractorUtil {
    // 正则表达式，匹配常见的章节标题模式
    private static final String CHAPTER_TITLE_PATTERN = "(?<=\\s|^)第[一二三四五六七八九十百千万零0-9]+章(?=\\s|$)";
    private static final FileService fileService = new FileService();

    public static boolean isChapterDocument(String documentContent) {
        Pattern pattern = Pattern.compile(CHAPTER_TITLE_PATTERN, Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(documentContent);
        return matcher.find();
    }

    public static List<String> sliceByChapter(String documentContent, Integer maxLength) {
        List<String> result = new ArrayList<>();
        Pattern pattern = Pattern.compile("(第[一二三四五六七八九十百千万零0-9]+章)(.*?)(?=(第[一二三四五六七八九十百千万零0-9]+章)|$)", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(documentContent);
        while (matcher.find()) {
            String chapterTitle = matcher.group(1);  // 章节标题，如“第X章”
            String chapterContent = matcher.group(2).trim();  // 章节内容

            String regex = "(第[一二三四五六七八九十百千万零0-9]+条)(.*?)(?=(第[一二三四五六七八九十百千万零0-9]+条)|$)";
            Pattern itemPattern = Pattern.compile(regex, Pattern.DOTALL);
            Matcher itemMatcher = itemPattern.matcher(chapterContent);

            StringBuilder currentChunk = new StringBuilder();
            String currentChapterTitle = chapterTitle;  // 当前章节的标题

            boolean found = false;
            while (itemMatcher.find()) {
                found = true;
                String itemTitle = "";
                String itemContent = "";
                if (itemMatcher.group(1) != null) {
                    itemTitle = itemMatcher.group(1).trim();
                }
                if (itemMatcher.group(2) != null) {
                    itemContent = itemMatcher.group(2).trim();
                }
                while (currentChunk.length() > maxLength) {
                    result.add(currentChapterTitle + " " + currentChunk.substring(0, maxLength));
                    currentChunk.delete(0, maxLength);
                }
                if (currentChunk.length() + itemTitle.length() + itemContent.length() > maxLength && currentChunk.length() > 0) {
                    result.add(currentChapterTitle + " " + currentChunk.toString().trim());
                    currentChunk.setLength(0);
                }

                currentChunk.append(itemTitle).append(" ").append(itemContent).append(" ");

                if (currentChunk.length() > maxLength) {
                    result.add(currentChapterTitle + " " + currentChunk.toString().trim());
                    currentChunk.setLength(0);
                }
            }
            if (!found) {
                currentChunk = new StringBuilder(chapterContent);
            }
            if (currentChunk.length() > 0) {
                while (currentChunk.length() > maxLength) {
                    result.add(currentChapterTitle + " " + currentChunk.substring(0, maxLength));
                    currentChunk.delete(0, maxLength);
                }
                result.add(currentChapterTitle + " " + currentChunk.toString().trim());
                currentChunk.setLength(0);
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

    public static void main(String[] args) throws IOException {
        String filePath = "C:\\Users\\ruiqing.luo\\Desktop\\rag调优\\130.1、XW2019152FJ.pdf";
//        String filePath = "C:\\Users\\ruiqing.luo\\Desktop\\rag调优\\2023年高速公路水毁修复工程（沈抚分公司）对比表.pdf"; // 替换为你的文件路径
//        String filePath = "C:\\Users\\ruiqing.luo\\Desktop\\rag调优\\察右中恩德风机故障处理手册.doc";
//        String filePath = "C:\\Users\\ruiqing.luo\\Desktop\\rag调优\\【最新版】DL∕T596-2021电力设备预防性试验规程.pdf";
//        String filePath = "C:\\Users\\ruiqing.luo\\Desktop\\规则测试.txt";

        String documentContent = fileService.getFileContent(new File(filePath));
        boolean isChapterDocument = isChapterDocument(documentContent);
        System.out.println("是否是章节类文档" + isChapterDocument);
        List<String> slices = sliceByChapter(documentContent, 512);
        for (String slice : slices) {
            System.out.println(slice);
            System.out.println();
        }

    }
}


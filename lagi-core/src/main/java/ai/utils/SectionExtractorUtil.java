package ai.utils;
import ai.common.pojo.FileChunkResponse;
import ai.vector.FileService;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SectionExtractorUtil {
    //小节类文档-
    private static final String CHAPTER_TITLE_PATTERN = "^(?<level>\\d+)(?:\\.(?<sublevel>\\d+(?:\\.\\d+)*))?\\s*(?<title>[^\\n]+)\\n(?:[^\\n]+\\n)*?(?<content>.*?)$";
    private static final FileService fileService = new FileService();
    public static boolean isChapterDocument(String documentContent) {
        Pattern pattern = Pattern.compile(CHAPTER_TITLE_PATTERN, Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(documentContent);
        return matcher.find();
    }

    public static List<String> sliceBySection(String document, Integer maxLength) {
        Pattern pattern = Pattern.compile(CHAPTER_TITLE_PATTERN, Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(document);
        List<StringBuilder> sections = new ArrayList<>();
        int lastMatchEnd = 0;

        while (matcher.find()) {
            StringBuilder sb = new StringBuilder();
            if (matcher.start() > lastMatchEnd) {
                String unmatchedText = document.substring(lastMatchEnd, matcher.start()).replaceAll("\\s+", "");;
                if (unmatchedText.trim().length() > 0){
                    sb.append(unmatchedText);
                }
            }
            String sectionContent = matcher.group(0).replaceAll("\\s+", "");
            sb.append(sectionContent);
            sections.add(sb);
            lastMatchEnd = matcher.end();
        }
        if (lastMatchEnd < document.length()) {
            String remainingText = document.substring(lastMatchEnd).replaceAll("\\s+", "");
            if (remainingText.trim().length() > 0){
                sections.add(new StringBuilder(remainingText));
            }
        }
        List<String> result = new ArrayList<>();
        StringBuilder section1 = new StringBuilder();
        for (StringBuilder section : sections) {
            if (section.length() > 0) {
                if (section.length() > maxLength){
                    if (section1.length() > 0) {
                        result.add(section1.toString().trim());
                        section1.setLength(0);
                    }

                    int start = 0;
                    while (start < section.length()) {
                        int end = Math.min(start + maxLength, section.length());
                        int lastSentenceEnd = Math.max(section.toString().lastIndexOf('.', end), section.toString().lastIndexOf('\n', end));
                        if (lastSentenceEnd != -1 && lastSentenceEnd > start) {
                            end = lastSentenceEnd + 1;
                        }
                        String text = section.substring(start, end).replaceAll("\\s+", " ").trim();
                        result.add(text);
                        start = end;
                    }
//                    while (section.length() > maxLength) {
//                        result.add( section.substring(0, maxLength));
//                        section.delete(0, maxLength);
//                    }
//                    result.add( section.toString().trim());
                    section.setLength(0);
                }
                if (section.length()>0 && (section.length()+section1.length()) <= maxLength){
                    section1.append(section+"/n");
                }else {
                    if (section1.length() > 0) {
                        result.add(section1.toString().trim());
                        section1.setLength(0);
                        section1.append(section);
                    }
                }
            }

        }
        return result;
    }

    public static List<FileChunkResponse.Document> getChunkDocument(String content, Integer chunkSize) {
        List<FileChunkResponse.Document> result = new ArrayList<>();
        List<String> slices = sliceBySection(content, chunkSize);
        for (String slice : slices) {
            FileChunkResponse.Document doc = new FileChunkResponse.Document();
            doc.setText(slice);
            result.add(doc);
        }
        return result;
    }

    public static void main(String[] args) throws IOException {
//        String filePath = "C:\\Users\\ruiqing.luo\\Desktop\\原始语料\\POC产品功能介绍手册20241108.pdf";
//        String filePath = "C:\\Users\\ruiqing.luo\\Desktop\\rag调优\\06-员工培训管理办法.pdf";
        String filePath ="C:\\Users\\ruiqing.luo\\Desktop\\rag调优\\材料\\北京地铁\\提供的资料-脱敏\\公司供电分公司电力安全工作规程\\公司电力安全工作规程.pdf";
//        String filePath = "C:\\Users\\ruiqing.luo\\Desktop\\原始语料\\关于印发《2025年第九届亚洲冬季运动会赛事指挥对讲终端领取使用指导意见》的通知.pdf";
        String documentContent = fileService.getFileContent(new File(filePath));
        boolean isChapterDocument = isChapterDocument(documentContent);
        System.out.println("是否是小节类文档" + isChapterDocument);
        List<FileChunkResponse.Document> result = getChunkDocument(documentContent, 512);

        for (FileChunkResponse.Document doc : result) {
            System.out.println(doc.getText());
            System.out.println();
        }
    }
}

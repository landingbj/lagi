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
                    while (section.length() > maxLength) {
                        result.add( section.substring(0, maxLength));
                        section.delete(0, maxLength);
                    }
                    result.add( section.toString().trim());
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
        String filePath = "C:\\Users\\ruiqing.luo\\Desktop\\rag调优\\察右中恩德风机故障处理手册.doc";
        String documentContent = fileService.getFileContent(new File(filePath));
        boolean isChapterDocument = isChapterDocument(documentContent);
        System.out.println("是否是章节类文档" + isChapterDocument);
        getChunkDocument(documentContent, 512);
    }
}

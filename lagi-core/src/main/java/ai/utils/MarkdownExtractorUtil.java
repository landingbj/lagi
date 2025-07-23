package ai.utils;

import ai.common.pojo.FileChunkResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MarkdownExtractorUtil {

    /**
     * 按照 Markdown 结构层次优先切片
     * @param content Markdown 内容字符串
     * @param chunkSize 切片最大长度
     * @return 切片列表
     */
    public static List<String> sliceByChapter(String content, int chunkSize) {
        List<String> slices = new ArrayList<>();

        if (content == null || content.isEmpty()) return slices;

        // 按一级标题分割
        List<String> level1Slices = splitByRegex(content, "^#\\s.*$", true);

        for (String slice : level1Slices) {
            if (slice.length() <= chunkSize) {
                slices.add(slice);
            } else {
                // 按二级标题再次分割
                List<String> level2Slices = splitByRegex(slice, "^##\\s.*$", true);
                for (String s : level2Slices) {
                    if (s.length() <= chunkSize) {
                        slices.add(s);
                    } else {
                        // 按列表项（- 或 *）分割
                        List<String> listSlices = splitByRegex(s, "^[*-]\\s.*$", true);
                        for (String ls : listSlices) {
                            if (ls.length() <= chunkSize) {
                                slices.add(ls);
                            } else {
                                // 按空行或单行分割
                                List<String> lineSlices = splitByRegex(ls, "\\n\\s*\\n", false);
                                slices.addAll(splitLinesToChunks(lineSlices, chunkSize));
                            }
                        }
                    }
                }
            }
        }

        return slices;
    }

    /**
     * 按正则表达式分割内容
     * @param content 内容
     * @param regex 正则表达式
     * @param isLineBased 是否为基于行的匹配（如 #、## 等）
     * @return 分割后的列表
     */
    private static List<String> splitByRegex(String content, String regex, boolean isLineBased) {
        List<String> result = new ArrayList<>();
        Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(content);

        int lastMatchEnd = 0;
        while (matcher.find()) {
            if (lastMatchEnd != matcher.start()) {
                result.add(content.substring(lastMatchEnd, matcher.start()));
            }
            lastMatchEnd = matcher.start();
        }
        if (lastMatchEnd < content.length()) {
            result.add(content.substring(lastMatchEnd));
        }

        return result;
    }

    /**
     * 将长文本按行分割并尝试压缩成 chunkSize 大小
     */
    private static List<String> splitLinesToChunks(List<String> lines, int chunkSize) {
        List<String> chunks = new ArrayList<>();
        StringBuilder currentChunk = new StringBuilder();

        for (String line : lines) {
            if (line.length() > chunkSize) {
                // 单行过长，按字符切片
                for (int i = 0; i < line.length(); i += chunkSize) {
                    int end = Math.min(i + chunkSize, line.length());
                    chunks.add(line.substring(i, end));
                }
            } else if (currentChunk.length() + line.length() + 1 > chunkSize && currentChunk.length() > 0) {
                chunks.add(currentChunk.toString());
                currentChunk.setLength(0);
                currentChunk.append(line);
            } else {
                if (currentChunk.length() > 0) currentChunk.append("\n");
                currentChunk.append(line);
            }
        }

        if (currentChunk.length() > 0) {
            chunks.add(currentChunk.toString());
        }

        return chunks;
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
}

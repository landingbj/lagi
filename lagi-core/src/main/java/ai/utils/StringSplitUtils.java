package ai.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class StringSplitUtils {

    // Define regex pattern for Chinese and English sentence-ending punctuation marks
    private static final Pattern SENTENCE_END_PATTERN = Pattern.compile("[.!?。！？；;]");

    /**
     * Split content into multiple chunks based on chunk size and separator rules
     *
     * @param chunkSize     Maximum length of each chunk
     * @param content       Content to be split
     * @param lineSeparator Whether to split by line separator first
     * @return List of split chunks
     */
    public static List<String> splitContentChunks(int chunkSize, String content, boolean lineSeparator) {
        List<String> result = new ArrayList<>();

        if (content == null || content.isEmpty() || chunkSize <= 0) {
            return result;
        }

        if (lineSeparator) {
            // Split by line separator first
            String[] lines = content.split("\r?\n");
            for (String line : lines) {
                if (!line.trim().isEmpty()) {
                    result.addAll(splitByChunkSize(chunkSize, line.trim()));
                }
            }
        } else {
            // Split directly by chunk size
            result.addAll(splitByChunkSize(chunkSize, content));
        }

        return result;
    }

    /**
     * Split text by chunk size, ensuring each chunk ends with sentence-ending punctuation
     *
     * @param chunkSize Maximum size of each chunk
     * @param text      Text to be split
     * @return List of split chunks
     */
    private static List<String> splitByChunkSize(int chunkSize, String text) {
        List<String> chunks = new ArrayList<>();

        if (text.length() <= chunkSize) {
            chunks.add(text);
            return chunks;
        }

        int start = 0;

        while (start < text.length()) {
            int end = Math.min(start + chunkSize, text.length());

            // If already at the end of text, directly add the remaining part
            if (end == text.length()) {
                chunks.add(text.substring(start));
                break;
            }

            // Find the last sentence-ending punctuation mark within the specified range
            int lastPunctuationIndex = findLastSentenceEnd(text, start, end);

            if (lastPunctuationIndex > start) {
                // Found sentence-ending punctuation, use it as the end
                chunks.add(text.substring(start, lastPunctuationIndex + 1));
                start = lastPunctuationIndex + 1;

                // Skip leading whitespace characters
                while (start < text.length() && Character.isWhitespace(text.charAt(start))) {
                    start++;
                }
            } else {
                // No sentence-ending punctuation found, look for the last space or punctuation mark
                int lastBreakIndex = findLastBreakPoint(text, start, end);

                if (lastBreakIndex > start) {
                    chunks.add(text.substring(start, lastBreakIndex));
                    start = lastBreakIndex;

                    // Skip leading whitespace characters
                    while (start < text.length() && Character.isWhitespace(text.charAt(start))) {
                        start++;
                    }
                } else {
                    // Force split (handle cases with no suitable break point)
                    chunks.add(text.substring(start, end));
                    start = end;
                }
            }
        }

        return chunks;
    }

    /**
     * Find the position of the last sentence-ending punctuation mark within the specified range
     */
    private static int findLastSentenceEnd(String text, int start, int end) {
        for (int i = end - 1; i >= start; i--) {
            if (SENTENCE_END_PATTERN.matcher(String.valueOf(text.charAt(i))).matches()) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Find the last suitable break point within the specified range (space, comma, etc.)
     */
    private static int findLastBreakPoint(String text, int start, int end) {
        for (int i = end - 1; i >= start; i--) {
            char c = text.charAt(i);
            if (Character.isWhitespace(c) || c == ',' || c == '，' || c == ':' || c == '：') {
                return i + 1;
            }
        }
        return -1;
    }

    // Test method
    public static void main(String[] args) {
        // Test case 1: Without line separator
        String content1 = "This is the first sentence. This is the second sentence! This is the third sentence? This is the fourth sentence; This is the fifth sentence.";
        List<String> result1 = splitContentChunks(40, content1, false);
        System.out.println("Test 1 results:");
        for (int i = 0; i < result1.size(); i++) {
            System.out.println("Chunk " + (i + 1) + ": " + result1.get(i));
        }

        // Test case 2: With line separator
        String content2 = "Line 1: This is the first sentence. This is the second sentence!\nLine 2: This is the third sentence? This is the fourth sentence;\nLine 3: This is the fifth sentence.";
        List<String> result2 = splitContentChunks(30, content2, true);
        System.out.println("\nTest 2 results:");
        for (int i = 0; i < result2.size(); i++) {
            System.out.println("Chunk " + (i + 1) + ": " + result2.get(i));
        }

        // Test case 3: Chinese content
        String content3 = "这是第一句话。这是第二句话！这是第三句话？这是第四句话；这是第五句话。";
        List<String> result3 = splitContentChunks(29, content3, false);
        System.out.println("\nTest 3 results:");
        for (int i = 0; i < result3.size(); i++) {
            System.out.println("Chunk " + (i + 1) + ": " + result3.get(i));
        }
    }
}
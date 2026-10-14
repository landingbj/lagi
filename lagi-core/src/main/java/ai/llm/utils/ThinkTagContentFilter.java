package ai.llm.utils;

/**
 * Removes model-generated {@code <think>...</think>} blocks from text output.
 * Instances retain enough state for tags split across streaming chunks.
 */
public final class ThinkTagContentFilter {
    private static final String OPEN_TAG = "<think>";
    private static final String CLOSE_TAG = "</think>";

    private String pending = "";
    private boolean insideThinkBlock;

    public String filter(String content) {
        if (content == null || content.isEmpty()) {
            return content;
        }
        pending += content;
        StringBuilder visible = new StringBuilder();
        while (!pending.isEmpty()) {
            if (insideThinkBlock) {
                int closeIndex = indexOfIgnoreCase(pending, CLOSE_TAG);
                if (closeIndex < 0) {
                    pending = keepPossibleTagPrefix(pending, CLOSE_TAG);
                    return visible.toString();
                }
                pending = pending.substring(closeIndex + CLOSE_TAG.length());
                insideThinkBlock = false;
                continue;
            }

            int openIndex = indexOfIgnoreCase(pending, OPEN_TAG);
            if (openIndex < 0) {
                String suffix = keepPossibleTagPrefix(pending, OPEN_TAG);
                visible.append(pending, 0, pending.length() - suffix.length());
                pending = suffix;
                return visible.toString();
            }
            visible.append(pending, 0, openIndex);
            pending = pending.substring(openIndex + OPEN_TAG.length());
            insideThinkBlock = true;
        }
        return visible.toString();
    }

    private static int indexOfIgnoreCase(String value, String target) {
        for (int i = 0; i <= value.length() - target.length(); i++) {
            if (value.regionMatches(true, i, target, 0, target.length())) {
                return i;
            }
        }
        return -1;
    }

    private static String keepPossibleTagPrefix(String value, String tag) {
        int maxLength = Math.min(value.length(), tag.length() - 1);
        for (int length = maxLength; length > 0; length--) {
            int start = value.length() - length;
            if (value.regionMatches(true, start, tag, 0, length)) {
                return value.substring(start);
            }
        }
        return "";
    }
}

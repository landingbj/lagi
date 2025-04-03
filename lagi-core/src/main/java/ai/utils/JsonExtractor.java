package ai.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JsonExtractor {
    /**
     * Extracts JSON strings from a given input string.
     * This method looks for content that appears to be valid JSON objects or arrays.
     *
     * @param input The string that may contain JSON content
     * @return A list of extracted JSON strings
     */
    public static List<String> extractJsonStrings(String input) {
        if (input == null || input.isEmpty()) {
            return new ArrayList<>();
        }

        List<String> jsonStrings = new ArrayList<>();

        // Pattern to find JSON objects (starting with { and ending with })
        Pattern objectPattern = Pattern.compile("\\{(?:[^{}]|(?:\\{[^{}]*\\}))*\\}");
        Matcher objectMatcher = objectPattern.matcher(input);

        while (objectMatcher.find()) {
            jsonStrings.add(objectMatcher.group());
        }

        // Pattern to find JSON arrays (starting with [ and ending with ])
        Pattern arrayPattern = Pattern.compile("\\[(?:[^\\[\\]]|(?:\\[[^\\[\\]]*\\]))*\\]");
        Matcher arrayMatcher = arrayPattern.matcher(input);

        while (arrayMatcher.find()) {
            jsonStrings.add(arrayMatcher.group());
        }

        return jsonStrings;
    }

    /**
     * Extract a single JSON string from input. Returns the first match.
     *
     * @param input The string that may contain JSON content
     * @return The first found JSON string or null if none is found
     */
    public static String extractFirstJsonString(String input) {
        List<String> results = extractJsonStrings(input);
        return results.isEmpty() ? null : results.get(0);
    }


    public static String extractJson(String input) {
        Stack<Character> stack = new Stack<>();
        int startIndex = -1;
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c == '{' || c == '[') {
                if (stack.isEmpty()) {
                    startIndex = i;
                }
                stack.push(c);
            } else if (c == '}' || c == ']') {
                if (!stack.isEmpty()) {
                    char top = stack.pop();
                    if ((top == '{' && c == '}') || (top == '[' && c == ']')) {
                        if (stack.isEmpty()) {
                            return input.substring(startIndex, i + 1);
                        }
                    }
                }
            }
        }
        return null;
    }




    // Example usage
    public static void main(String[] args) {
        String input = "Some text {\"data\": [{\"key\": \"value\"}]} more text";
        String json = extractJson(input);
        if (json != null) {
            System.out.println("Extracted JSON: " + json);
        } else {
            System.out.println("No valid JSON found.");
        }
    }
}
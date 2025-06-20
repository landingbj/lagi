package ai.utils;

import java.util.ArrayList;
import java.util.List;
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

        return jsonStrings;
    }

    public static List<String> extractJsonArrayStrings(String input) {
        if (input == null || input.isEmpty()) {
            return new ArrayList<>();
        }
        List<String> jsonStrings = new ArrayList<>();
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

    public static String extractFirstJsonArray(String input) {
        List<String> results = extractJsonArrayStrings(input);
        return results.isEmpty() ? null : results.get(0);
    }

    // Example usage
    public static void main(String[] args) {
        String testString = "Some text before {\"name\": \"John\", \"age\": 30} and some text after. " +
                "Also here's an array [1, 2, 3, 4] and another object {\"city\": \"New York\"}";

        List<String> jsonStrings = extractJsonStrings(testString);
        System.out.println("Found " + jsonStrings.size() + " JSON strings:");
        for (String json : jsonStrings) {
            System.out.println(json);
        }

        List<String> jsonArrayStrings = extractJsonArrayStrings(testString);
        System.out.println("Found " + jsonArrayStrings.size() + " JSON arrays:");
        for (String json : jsonArrayStrings) {
            System.out.println(json);
        }
    }
}
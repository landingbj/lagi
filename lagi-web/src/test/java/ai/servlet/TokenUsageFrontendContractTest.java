package ai.servlet;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertTrue;

class TokenUsageFrontendContractTest {

    @Test
    void userColumnPrefersResolvedDisplayNameAndFallsBackToUserId() throws Exception {
        String source = readWebAsset("tokenUsage.js");
        String picker = functionBlock(source, "function pickTokenUsageUserName");
        String renderer = functionBlock(source, "function renderTokenSessionTableFromApi");

        assertTrue(picker.contains("row.userDisplayName"));
        assertTrue(picker.contains("row.userId"));
        assertTrue(renderer.contains("pickTokenUsageUserName(row)"));
        assertTrue(renderer.contains("escapeTokenUsageHtml(userName)"));
    }

    private String readWebAsset(String name) throws Exception {
        Path path = Paths.get("src", "main", "webapp", "js", name);
        return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
    }

    private String functionBlock(String source, String startMarker) {
        int start = source.indexOf(startMarker);
        assertTrue(start >= 0, "Missing marker: " + startMarker);
        int bodyStart = source.indexOf('{', start + startMarker.length());
        assertTrue(bodyStart > start, "Missing function body: " + startMarker);
        int depth = 0;
        for (int i = bodyStart; i < source.length(); i++) {
            char current = source.charAt(i);
            if (current == '{') {
                depth++;
            } else if (current == '}' && --depth == 0) {
                return source.substring(start, i + 1);
            }
        }
        throw new AssertionError("Unclosed function body: " + startMarker);
    }
}

package ai.migrate.service;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;

/**
 * Parses the user identity returned by the existing Landing API-key service.
 * The service has used more than one response envelope over time, so the
 * parser accepts the known success shapes while refusing ambiguous responses.
 */
public final class LandingApiKeyIdentityResolver {
    private LandingApiKeyIdentityResolver() {
    }

    public static String extractUserId(String body) throws IOException {
        if (isBlank(body)) {
            throw new IOException("Landing user ID response is empty");
        }
        final JsonElement root;
        try {
            root = new JsonParser().parse(body);
        } catch (RuntimeException e) {
            throw new IOException("Landing user ID response is not valid JSON", e);
        }
        if (root == null || root.isJsonNull()) {
            throw new IOException("Landing user ID response is empty");
        }
        if (root.isJsonPrimitive()) {
            return requiredText(root, "Landing user ID response is empty");
        }
        if (!root.isJsonObject()) {
            throw new IOException("Landing user ID response has an unsupported format");
        }

        JsonObject response = root.getAsJsonObject();
        String status = text(response, "status");
        if (status != null && !"success".equalsIgnoreCase(status)) {
            throw new IOException(firstNonBlank(text(response, "msg"), text(response, "message"),
                    "Landing API key was rejected"));
        }

        String userId = userId(response);
        if (userId != null) {
            return userId;
        }
        for (String field : new String[]{"data", "result", "body"}) {
            if (!response.has(field) || response.get(field).isJsonNull()) {
                continue;
            }
            JsonElement nested = response.get(field);
            if (nested.isJsonPrimitive()) {
                return requiredText(nested, "Landing user ID response is empty");
            }
            if (nested.isJsonObject()) {
                userId = userId(nested.getAsJsonObject());
                if (userId != null) {
                    return userId;
                }
            }
        }
        throw new IOException("Landing user ID was not present in the response");
    }

    private static String userId(JsonObject object) {
        for (String field : new String[]{"userId", "user_id", "uid", "id"}) {
            String value = text(object, field);
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    private static String requiredText(JsonElement element, String message) throws IOException {
        if (element == null || !element.isJsonPrimitive()) {
            throw new IOException(message);
        }
        String value = element.getAsString();
        if (isBlank(value)) {
            throw new IOException(message);
        }
        return value.trim();
    }

    private static String text(JsonObject object, String field) {
        if (object == null || !object.has(field) || object.get(field).isJsonNull()
                || !object.get(field).isJsonPrimitive()) {
            return null;
        }
        String value = object.get(field).getAsString();
        return isBlank(value) ? null : value.trim();
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (!isBlank(value)) {
                return value.trim();
            }
        }
        return null;
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}

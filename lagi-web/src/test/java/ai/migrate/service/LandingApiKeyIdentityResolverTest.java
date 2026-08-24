package ai.migrate.service;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LandingApiKeyIdentityResolverTest {

    @Test
    void resolvesKnownLandingResponseEnvelopes() throws Exception {
        assertEquals("user-1", LandingApiKeyIdentityResolver.extractUserId("{\"data\":{\"userId\":\"user-1\"}}"));
        assertEquals("user-2", LandingApiKeyIdentityResolver.extractUserId("{\"status\":\"success\",\"data\":\"user-2\"}"));
        assertEquals("user-3", LandingApiKeyIdentityResolver.extractUserId("{\"code\":0,\"result\":{\"id\":\"user-3\"}}"));
    }

    @Test
    void rejectsFailureAndMissingIdentity() {
        assertThrows(IOException.class,
                () -> LandingApiKeyIdentityResolver.extractUserId("{\"status\":\"failed\",\"msg\":\"invalid key\"}"));
        assertThrows(IOException.class,
                () -> LandingApiKeyIdentityResolver.extractUserId("{\"status\":\"success\",\"data\":{}}"));
    }
}

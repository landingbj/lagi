package ai.migrate.service;

import ai.migrate.service.BoxLandingAccountResolver.BoxBillingException;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BoxLandingAccountResolverTest {

    @Test
    void resolvesAndCachesTheConfiguredBoxKeyOwner() throws Exception {
        AtomicInteger lookupCalls = new AtomicInteger();
        BoxLandingAccountResolver resolver = new BoxLandingAccountResolver(
                new BoxLandingAccountResolver.LandingKeyProvider() {
                    @Override
                    public String getApiKey() {
                        return "sk-box-key";
                    }
                }, new BoxLandingAccountResolver.LandingAccountLookup() {
                    @Override
                    public String resolveAccountId(String apiKey) {
                        assertEquals("sk-box-key", apiKey);
                        lookupCalls.incrementAndGet();
                        return " remote-owner ";
                    }
                });

        assertEquals("remote-owner", resolver.resolveAccountId());
        assertEquals("remote-owner", resolver.resolveAccountId());
        assertEquals(1, lookupCalls.get());
    }

    @Test
    void refusesAnUnconfiguredBoxKey() {
        BoxLandingAccountResolver resolver = new BoxLandingAccountResolver(
                new BoxLandingAccountResolver.LandingKeyProvider() {
                    @Override
                    public String getApiKey() {
                        return " ";
                    }
                }, new BoxLandingAccountResolver.LandingAccountLookup() {
                    @Override
                    public String resolveAccountId(String apiKey) {
                        throw new AssertionError("lookup must not be called without a box key");
                    }
                });

        BoxBillingException exception = assertThrows(BoxBillingException.class, resolver::resolveAccountId);
        assertEquals("BOX_REMOTE_KEY_NOT_CONFIGURED", exception.getCode());
    }

    @Test
    void wrapsSaasAccountLookupFailures() {
        BoxLandingAccountResolver resolver = new BoxLandingAccountResolver(
                new BoxLandingAccountResolver.LandingKeyProvider() {
                    @Override
                    public String getApiKey() {
                        return "sk-box-key";
                    }
                }, new BoxLandingAccountResolver.LandingAccountLookup() {
                    @Override
                    public String resolveAccountId(String apiKey) throws IOException {
                        throw new IOException("upstream unavailable");
                    }
                });

        BoxBillingException exception = assertThrows(BoxBillingException.class, resolver::resolveAccountId);
        assertEquals("BOX_REMOTE_ACCOUNT_UNAVAILABLE", exception.getCode());
    }
}

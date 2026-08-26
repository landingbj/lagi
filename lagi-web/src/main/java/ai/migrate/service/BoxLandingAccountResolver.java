package ai.migrate.service;

import ai.common.pojo.Backend;
import ai.config.ContextLoader;
import ai.utils.LagiGlobal;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** Resolves the SaaS billing account that owns this box's configured Landing key. */
public class BoxLandingAccountResolver {
    private final LandingKeyProvider keyProvider;
    private final LandingAccountLookup accountLookup;

    private volatile String cachedApiKey;
    private volatile String cachedAccountId;

    public BoxLandingAccountResolver() {
        this(new LandingKeyProvider() {
            @Override
            public String getApiKey() {
                String configuredKey = findConfiguredLandingKey();
                if (configuredKey != null) {
                    return configuredKey;
                }
                // Kept for legacy deployments that initialized this value before
                // the active configuration became available.
                return trim(LagiGlobal.getLandingApikey());
            }
        }, new LandingAccountLookup() {
            private final ApiKeyService apiKeyService = new ApiKeyService();

            @Override
            public String resolveAccountId(String apiKey) throws IOException {
                return apiKeyService.resolveLandingUserId(apiKey);
            }
        });
    }

    BoxLandingAccountResolver(LandingKeyProvider keyProvider, LandingAccountLookup accountLookup) {
        this.keyProvider = keyProvider;
        this.accountLookup = accountLookup;
    }

    public String resolveAccountId() throws BoxBillingException {
        final String apiKey;
        try {
            apiKey = trim(keyProvider.getApiKey());
        } catch (BoxKeyConfigurationException e) {
            throw new BoxBillingException(e.getCode(), e.getMessage(), e);
        }
        if (apiKey == null) {
            throw new BoxBillingException("BOX_REMOTE_KEY_NOT_CONFIGURED",
                    "the box Landing API key is not configured");
        }
        String accountId = cachedAccountId;
        if (apiKey.equals(cachedApiKey) && accountId != null) {
            return accountId;
        }
        synchronized (this) {
            if (apiKey.equals(cachedApiKey) && cachedAccountId != null) {
                return cachedAccountId;
            }
            try {
                accountId = trim(accountLookup.resolveAccountId(apiKey));
            } catch (IOException e) {
                throw new BoxBillingException("BOX_REMOTE_ACCOUNT_UNAVAILABLE",
                        "unable to resolve the SaaS account for this box", e);
            }
            if (accountId == null) {
                throw new BoxBillingException("BOX_REMOTE_ACCOUNT_UNAVAILABLE",
                        "the configured Landing API key has no SaaS account");
            }
            cachedApiKey = apiKey;
            cachedAccountId = accountId;
            return accountId;
        }
    }

    interface LandingKeyProvider {
        String getApiKey();
    }

    interface LandingAccountLookup {
        String resolveAccountId(String apiKey) throws IOException;
    }

    public static class BoxBillingException extends IOException {
        private final String code;

        BoxBillingException(String code, String message) {
            super(message);
            this.code = code;
        }

        BoxBillingException(String code, String message, Throwable cause) {
            super(message, cause);
            this.code = code;
        }

        public String getCode() {
            return code;
        }
    }

    private static String trim(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    /**
     * Newer configurations may put the enabled Landing key in {@code api_keys}
     * instead of the legacy single {@code api_key} field. The payment flow must
     * use the same configured key as model routing, while refusing an ambiguous
     * configuration rather than selecting an arbitrary shared account.
     */
    private static String findConfiguredLandingKey() {
        if (ContextLoader.configuration == null || ContextLoader.configuration.getModels() == null) {
            return null;
        }
        Set<String> candidates = new LinkedHashSet<String>();
        List<Backend> models = ContextLoader.configuration.getModels();
        for (Backend model : models) {
            if (model == null || !Boolean.TRUE.equals(model.getEnable())
                    || !LagiGlobal.LLM_TYPE_LANDING.equalsIgnoreCase(model.getType())) {
                continue;
            }
            addLandingKey(candidates, model.getApiKey());
            if (model.getApiKeys() != null) {
                for (String apiKey : model.getApiKeys()) {
                    addLandingKey(candidates, apiKey);
                }
            }
        }
        if (candidates.size() == 1) {
            return candidates.iterator().next();
        }
        if (candidates.size() > 1) {
            throw new BoxKeyConfigurationException("BOX_REMOTE_KEY_AMBIGUOUS",
                    "multiple Landing API keys are configured for this box");
        }
        return null;
    }

    private static void addLandingKey(Set<String> candidates, String apiKey) {
        String value = trim(apiKey);
        if (value != null && value.startsWith("sk-") && !"your-api-key".equalsIgnoreCase(value)) {
            candidates.add(value);
        }
    }

    private static class BoxKeyConfigurationException extends RuntimeException {
        private final String code;

        private BoxKeyConfigurationException(String code, String message) {
            super(message);
            this.code = code;
        }

        private String getCode() {
            return code;
        }
    }
}

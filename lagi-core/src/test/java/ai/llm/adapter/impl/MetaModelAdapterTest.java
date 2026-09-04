package ai.llm.adapter.impl;

import ai.llm.responses.ResponseProtocolUtil;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MetaModelAdapterTest {

    @Test
    void usesMetaChatCompletionsEndpointByDefault() {
        MetaModelAdapter adapter = new MetaModelAdapter();

        assertEquals("https://api.meta.ai/v1/chat/completions", adapter.getApiAddress());
        assertEquals("https://api.meta.ai/v1/responses", adapter.getResponsesApiAddress());
    }

    @Test
    void allowsConfiguredEndpointAndResponseProtocolValidation() {
        MetaModelAdapter adapter = new MetaModelAdapter();
        adapter.setApiAddress("https://gateway.example/v1/responses");

        assertEquals("https://gateway.example/v1/responses", adapter.getApiAddress());
        assertTrue(ResponseProtocolUtil.supportsResponses(MetaModelAdapter.class.getName()));
    }
}

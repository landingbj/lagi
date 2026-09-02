package ai.llm.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LLMErrorConstantsTest {

    @Test
    void mapsInternalErrorCodesToValidHttpStatuses() {
        assertEquals(400, LLMErrorConstants.toHttpStatus(LLMErrorConstants.INVALID_REQUEST_ERROR));
        assertEquals(502, LLMErrorConstants.toHttpStatus(LLMErrorConstants.OTHER_ERROR));
        assertEquals(504, LLMErrorConstants.toHttpStatus(LLMErrorConstants.TIME_OUT));
        assertEquals(503, LLMErrorConstants.toHttpStatus(LLMErrorConstants.NO_AVAILABLE_MODEL));
        assertEquals(500, LLMErrorConstants.toHttpStatus(999));
    }
}

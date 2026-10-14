package ai.llm.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ThinkTagContentFilterTest {

    @Test
    void removesThinkBlockFromACompleteResponse() {
        ThinkTagContentFilter filter = new ThinkTagContentFilter();

        assertEquals("最终回答", filter.filter("<think>internal reasoning</think>最终回答"));
    }

    @Test
    void removesThinkBlockWhenBothTagsAreSplitAcrossStreamingChunks() {
        ThinkTagContentFilter filter = new ThinkTagContentFilter();

        assertEquals("开头", filter.filter("开头<th"));
        assertEquals("", filter.filter("ink>内部推"));
        assertEquals("", filter.filter("理</th"));
        assertEquals("最终回答", filter.filter("ink>最终回答"));
    }

    @Test
    void preservesNormalTextThatContainsAnAngleBracket() {
        ThinkTagContentFilter filter = new ThinkTagContentFilter();

        assertEquals("a < b", filter.filter("a < b"));
    }

    @Test
    void removesMultipleThinkBlocksCaseInsensitively() {
        ThinkTagContentFilter filter = new ThinkTagContentFilter();

        assertEquals("A B C", filter.filter("A <THINK>one</THINK>B<think>two</think> C"));
    }
}

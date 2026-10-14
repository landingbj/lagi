package ai.servlet.api;

import ai.openai.pojo.ChatCompletionChoice;
import ai.openai.pojo.ChatCompletionResult;
import ai.openai.pojo.ChatMessage;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class StreamCompletionTerminatorTest {
    @Test
    void addsStopWhenContentStreamCompletesWithoutFinishReason() {
        StreamCompletionTerminator terminator = new StreamCompletionTerminator();
        terminator.observe(chunk(0, "answer", null));

        ChatCompletionResult terminal = terminator.missingFinishChunk();

        assertEquals("stop", terminal.getChoices().get(0).getFinish_reason());
        assertEquals(0, terminal.getChoices().get(0).getIndex());
        assertNull(terminal.getChoices().get(0).getDelta().getContent());
    }

    @Test
    void doesNotDuplicateAnUpstreamFinishReason() {
        StreamCompletionTerminator terminator = new StreamCompletionTerminator();
        terminator.observe(chunk(0, "answer", "stop"));

        assertNull(terminator.missingFinishChunk());
    }

    @Test
    void completesOnlyUnfinishedChoices() {
        StreamCompletionTerminator terminator = new StreamCompletionTerminator();
        ChatCompletionResult result = new ChatCompletionResult();
        result.setChoices(Arrays.asList(
                choice(0, "first", "stop"),
                choice(1, "second", null)));
        terminator.observe(result);

        ChatCompletionResult terminal = terminator.missingFinishChunk();

        assertEquals(1, terminal.getChoices().size());
        assertEquals(1, terminal.getChoices().get(0).getIndex());
        assertEquals("stop", terminal.getChoices().get(0).getFinish_reason());
    }

    @Test
    void doesNotInventACompletionForAnEmptyStream() {
        StreamCompletionTerminator terminator = new StreamCompletionTerminator();
        terminator.observe(new ChatCompletionResult());

        assertNull(terminator.missingFinishChunk());
    }

    private ChatCompletionResult chunk(int index, String content, String finishReason) {
        ChatCompletionResult result = new ChatCompletionResult();
        result.setChoices(Collections.singletonList(choice(index, content, finishReason)));
        return result;
    }

    private ChatCompletionChoice choice(int index, String content, String finishReason) {
        ChatMessage delta = new ChatMessage();
        delta.setContent(content);
        ChatCompletionChoice choice = new ChatCompletionChoice();
        choice.setIndex(index);
        choice.setDelta(delta);
        choice.setFinish_reason(finishReason);
        return choice;
    }
}

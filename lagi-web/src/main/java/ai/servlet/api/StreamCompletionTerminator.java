package ai.servlet.api;

import ai.openai.pojo.ChatCompletionChoice;
import ai.openai.pojo.ChatCompletionResult;
import ai.openai.pojo.ChatMessage;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Keeps the OpenAI-compatible SSE stream well formed when an upstream provider
 * completes without its own finish chunk.
 */
final class StreamCompletionTerminator {
    private final Set<Integer> seenChoiceIndexes = new LinkedHashSet<>();
    private final Set<Integer> finishedChoiceIndexes = new LinkedHashSet<>();
    private ChatCompletionResult template;

    void observe(ChatCompletionResult result) {
        if (result == null || result.getChoices() == null || result.getChoices().isEmpty()) {
            return;
        }
        template = result;
        for (ChatCompletionChoice choice : result.getChoices()) {
            if (choice == null) {
                continue;
            }
            seenChoiceIndexes.add(choice.getIndex());
            if (choice.getFinish_reason() != null) {
                finishedChoiceIndexes.add(choice.getIndex());
            }
        }
    }

    ChatCompletionResult missingFinishChunk() {
        if (template == null || seenChoiceIndexes.isEmpty()) {
            return null;
        }
        List<ChatCompletionChoice> choices = new ArrayList<>();
        for (Integer index : seenChoiceIndexes) {
            if (finishedChoiceIndexes.contains(index)) {
                continue;
            }
            ChatCompletionChoice choice = new ChatCompletionChoice();
            choice.setIndex(index);
            choice.setDelta(new ChatMessage());
            choice.setFinish_reason("stop");
            choices.add(choice);
        }
        if (choices.isEmpty()) {
            return null;
        }
        ChatCompletionResult terminal = new ChatCompletionResult();
        terminal.setId(template.getId());
        terminal.setObject(template.getObject());
        terminal.setCreated(template.getCreated());
        terminal.setModel(template.getModel());
        terminal.setSystem_fingerprint(template.getSystem_fingerprint());
        terminal.setChoices(choices);
        return terminal;
    }
}

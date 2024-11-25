package ai.llm.consumer;

import ai.common.exception.RRException;
import ai.llm.pojo.llmScheduleData;
import ai.llm.service.CompletionsService;
import ai.mr.pipeline.Consumer;
import ai.openai.pojo.ChatCompletionRequest;
import ai.openai.pojo.ChatCompletionResult;
import io.reactivex.Observable;


public class LlmCompletionConsumer implements Consumer<llmScheduleData> {

    private final CompletionsService completionsService;

    public LlmCompletionConsumer(CompletionsService completionsService) {
        this.completionsService = completionsService;
    }

    @Override
    public void init() {
    }

    @Override
    public void consume(llmScheduleData data) throws Exception {
        ChatCompletionRequest request = data.getRequest();
        if(Boolean.TRUE.equals(request.getStream())) {
            try {
                Observable<ChatCompletionResult> completions = completionsService.streamCompletions(request, data.getIndexSearchDataList());
                data.setStreamResult(completions);
            } catch (RRException e) {
                data.setException(e);
            } finally {
                data.getLatch().countDown();
            }
        } else {
            try {
                ChatCompletionResult completions = completionsService.completions(request, data.getIndexSearchDataList());
                data.setResult(completions);
            } catch (RRException e) {
                data.setException(e);
            } finally {
                data.getLatch().countDown();
            }
        }
    }

    @Override
    public void cleanup() {

    }
}

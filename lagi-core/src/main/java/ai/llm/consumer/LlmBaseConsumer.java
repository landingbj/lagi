package ai.llm.consumer;

import ai.common.ModelService;
import ai.common.exception.RRException;
import ai.llm.adapter.ILlmAdapter;
import ai.llm.pojo.llmScheduleData;
import ai.llm.service.CompletionsService;
import ai.mr.pipeline.Consumer;
import ai.openai.pojo.ChatCompletionRequest;
import ai.openai.pojo.ChatCompletionResult;
import io.reactivex.Observable;


public class LlmBaseConsumer implements Consumer<llmScheduleData> {

    private ILlmAdapter adapter;
    public LlmBaseConsumer(ILlmAdapter adapter) {
        this.adapter = adapter;
    }

    @Override
    public void init() {
    }

    @Override
    public void consume(llmScheduleData data) throws Exception {
        String model = data.getRequest().getModel();
        ModelService modelService = (ModelService) adapter;
        if(!modelService.getModel().equals(model)){
            return;
        }
        ChatCompletionRequest request = data.getRequest();
        try {
            if(Boolean.TRUE.equals(request.getStream())) {
                Observable<ChatCompletionResult> completions = adapter.streamCompletions(data.getRequest());
                data.setStreamResult(completions);
            } else {
                ChatCompletionResult completions = adapter.completions(data.getRequest());
                data.setResult(completions);
            }
            CompletionsService.unfreezeAdapter(adapter);
        } catch (RRException e) {
            data.setException(e);
            CompletionsService.freezingAdapter(adapter);
        } finally {
            data.getLatch().countDown();
        }
    }

    @Override
    public void cleanup() {

    }
}

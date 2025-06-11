package ai.intent.mapper;

import ai.intent.IntentGlobal;
import ai.intent.IntentService;
import ai.intent.impl.SampleIntentServiceImpl;
import ai.intent.pojo.IntentDetectParam;
import ai.intent.pojo.IntentDetectResult;
import ai.intent.pojo.IntentResult;
import ai.mr.IMapper;
import ai.mr.mapper.BaseMapper;
import ai.openai.pojo.ChatCompletionRequest;
import ai.qa.AiGlobalQA;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;


@Setter
@Slf4j
public class ModalDetectMapper extends BaseMapper implements IMapper {
    private static final Double priority = 50d;
    private IntentService sampleIntentService = new SampleIntentServiceImpl();

    @Override
    public List<?> myMapping() {
        List<Object> result = new ArrayList<>();
        IntentDetectParam param = (IntentDetectParam) this.getParameters().get(IntentGlobal.MAPPER_INTENT_PARAM);
        ChatCompletionRequest request = param.getLlmRequest();
        IntentResult intentModal = sampleIntentService.detectIntent(request);
        IntentDetectResult intentResult = new IntentDetectResult();
        intentResult.setModal(intentModal);
        result.add(AiGlobalQA.M_LIST_RESULT_TEXT, intentResult);
        result.add(AiGlobalQA.M_LIST_RESULT_PRIORITY, priority);
        return result;
    }
}

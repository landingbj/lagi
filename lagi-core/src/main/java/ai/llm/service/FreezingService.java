package ai.llm.service;

import ai.common.ModelService;
import ai.config.ContextLoader;
import ai.config.pojo.Policy;
import ai.llm.adapter.ILlmAdapter;
import ai.llm.utils.CacheManager;
import ai.llm.utils.LLMErrorConstants;
import cn.hutool.core.bean.BeanUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FreezingService {
    private static final Logger logger = LoggerFactory.getLogger(FreezingService.class);

    public static Policy getPolicy() {
        Policy policy = BeanUtil.copyProperties(ContextLoader.configuration.getFunctions().getChat(), Policy.class);
        return policy;
    }

    public static boolean notFreezingAdapter(ILlmAdapter adapter) {
        ModelService modelService = (ModelService) adapter;
        Integer freezingCount = CacheManager.getInstance().getCount(modelService.getModel());
        if (freezingCount >= getPolicy().getMaxGen()) {
            return false;
        }
        return CacheManager.getInstance().get(modelService.getModel());
    }

    public static void freezingAdapterByErrorCode(ILlmAdapter adapter, int errorCode) {
        if (errorCode == LLMErrorConstants.PERMISSION_DENIED_ERROR
                || errorCode == LLMErrorConstants.RESOURCE_NOT_FOUND_ERROR
                || errorCode == LLMErrorConstants.INVALID_AUTHENTICATION_ERROR) {
            freezingAdapter(adapter);
        }
    }

    public static void freezingAdapter(ILlmAdapter adapter) {
        ModelService modelService = (ModelService) adapter;
        String model = modelService.getModel();
        if (CacheManager.getInstance().get(model)) {
            CacheManager.getInstance().put(modelService.getModel(), false);
            logger.error("The  model {} has been blocked.", modelService.getModel());
        }
    }

    public static void unfreezeAdapter(ILlmAdapter adapter) {
        ModelService modelService = (ModelService) adapter;
        String model = modelService.getModel();
        CacheManager.getInstance().removeCount(model);
        CacheManager.getInstance().remove(model);
    }
}

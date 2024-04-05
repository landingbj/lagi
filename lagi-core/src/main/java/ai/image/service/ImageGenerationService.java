package ai.image.service;

import ai.image.adapter.IImageGenerationAdapter;
import ai.image.adapter.impl.LandingImageGenerationAdapter;
import ai.common.pojo.Backend;
import ai.common.pojo.Configuration;
import ai.common.pojo.ImageGeneration;
import ai.common.pojo.ImageGenerationRequest;
import ai.common.pojo.ImageGenerationResult;
import ai.utils.LagiGlobal;

public class ImageGenerationService {
    private Configuration config;

    public ImageGenerationService(Configuration config) {
        this.config = config;
    }

    public ImageGenerationResult generations(ImageGenerationRequest request) {
        IImageGenerationAdapter adapter = getAdapter(config.getImageGeneration());
        ImageGenerationResult result = adapter.generations(request);
        return result;
    }

    private IImageGenerationAdapter getAdapter(ImageGeneration config) {
        IImageGenerationAdapter adapter = null;
        int maxPriority = Integer.MIN_VALUE;
        for (Backend backend : config.getBackends()) {
            if (backend.getEnable() && backend.getPriority() > maxPriority) {
                adapter = getAdapter(backend.getType());
            }
        }
        return adapter;
    }

    private IImageGenerationAdapter getAdapter(String type) {
        IImageGenerationAdapter adapter = null;
        if (type.equals(LagiGlobal.IMAGE_TYPE_LANDING)) {
            adapter = new LandingImageGenerationAdapter();
        } else if (type.equals(LagiGlobal.IMAGE_TYPE_ALIBABA)) {
            // adapter = new SimulatingTreeMapper();
        }
        return adapter;
    }
}

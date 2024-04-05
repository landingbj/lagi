package ai.lagi.service;

import ai.lagi.adapter.IImageGenerationAdapter;
import ai.lagi.adapter.impl.LandingImageGenerationAdapter;
import ai.migrate.pojo.Backend;
import ai.migrate.pojo.Configuration;
import ai.migrate.pojo.ImageGeneration;
import ai.migrate.pojo.ImageGenerationRequest;
import ai.migrate.pojo.ImageGenerationResult;
import ai.utils.LagiGlobal;

public class ImageGenerationService {
    private Configuration config;

    public ImageGenerationService(Configuration config) {
        this.config = config;
    }

    public ImageGenerationResult generations(ImageGenerationRequest request) {
        IImageGenerationAdapter adapter = getAdapter(config.getImage_generation());
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

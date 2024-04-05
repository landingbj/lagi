package ai.lagi.adapter;

import ai.migrate.pojo.ImageGenerationRequest;
import ai.migrate.pojo.ImageGenerationResult;

public interface IImageGenerationAdapter {
    ImageGenerationResult generations(ImageGenerationRequest request);
}

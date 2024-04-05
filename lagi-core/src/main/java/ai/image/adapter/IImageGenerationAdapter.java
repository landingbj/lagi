package ai.image.adapter;

import ai.common.pojo.ImageGenerationRequest;
import ai.common.pojo.ImageGenerationResult;

public interface IImageGenerationAdapter {
    ImageGenerationResult generations(ImageGenerationRequest request);
}

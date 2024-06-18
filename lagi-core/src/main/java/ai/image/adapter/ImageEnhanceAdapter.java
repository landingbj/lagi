package ai.image.adapter;

import ai.common.pojo.ImageEnhanceResult;
import ai.image.pojo.ImageEnhanceRequest;

public interface ImageEnhanceAdapter {
    ImageEnhanceResult enhance(ImageEnhanceRequest imageEnhanceRequest);
}

package ai.image.adapter;

import ai.common.pojo.ImageEnhanceResult;

public interface ImageEnhanceAdapter {
    ImageEnhanceResult enhance(String imageUrl);
}

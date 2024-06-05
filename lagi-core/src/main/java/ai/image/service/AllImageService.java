package ai.image.service;

import ai.common.pojo.*;
import ai.image.adapter.IImage2TextAdapter;
import ai.image.adapter.IImageGenerationAdapter;
import ai.image.adapter.ImageEnhanceAdapter;
import ai.managers.Image2TextManger;
import ai.managers.ImageEnhanceManager;
import ai.managers.ImageGenerationManager;

import java.util.List;

public class AllImageService implements IImage2TextAdapter, ImageEnhanceAdapter, IImageGenerationAdapter {

    @Override
    public ImageToTextResponse toText(FileRequest param) {
        List<IImage2TextAdapter> adapters = Image2TextManger.getInstance().getAdapters();
        for (IImage2TextAdapter adapter : adapters) {
            if(adapter != null) {
                return adapter.toText(param);
            }
        }
        return null;
    }

    @Override
    public ImageEnhanceResult enhance(String imageUrl) {
        List<ImageEnhanceAdapter> adapters = ImageEnhanceManager.getInstance().getAdapters();
        for (ImageEnhanceAdapter adapter : adapters) {
            if(adapter != null) {
                return adapter.enhance(imageUrl);
            }
        }
        return null;
    }

    @Override
    public ImageGenerationResult generations(ImageGenerationRequest request) {
        List<IImageGenerationAdapter> adapters = ImageGenerationManager.getInstance().getAdapters();
        for (IImageGenerationAdapter adapter : adapters) {
            if(adapter != null) {
                return adapter.generations(request);
            }
        }
        return null;
    }
}

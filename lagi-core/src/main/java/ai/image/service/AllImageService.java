package ai.image.service;

import ai.common.pojo.FileRequest;
import ai.common.pojo.ImageToTextResponse;
import ai.image.ImageManager;
import ai.image.adapter.IImage2TextAdapter;

public class AllImageService implements IImage2TextAdapter {

    @Override
    public ImageToTextResponse toText(FileRequest param) {
        return ImageManager.getImage2TextAdapter().toText(param);
    }

}

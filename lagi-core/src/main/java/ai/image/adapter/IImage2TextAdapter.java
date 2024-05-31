package ai.image.adapter;

import ai.common.pojo.FileRequest;
import ai.common.pojo.ImageToTextResponse;

public interface IImage2TextAdapter  {
    ImageToTextResponse toText(FileRequest param);
}

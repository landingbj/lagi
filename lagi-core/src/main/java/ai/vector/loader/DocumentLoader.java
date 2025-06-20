package ai.vector.loader;

import ai.common.pojo.FileChunkResponse;
import ai.vector.loader.pojo.SplitConfig;

import java.util.List;

public interface DocumentLoader {
//    List<FileChunkResponse.Document> load(String path, SplitConfig splitConfig);

    List<List<FileChunkResponse.Document>> load(String path, SplitConfig splitConfig);
}

package ai.vector.loader.impl;

import ai.common.pojo.FileChunkResponse;
import ai.utils.*;
import ai.vector.FileService;
import ai.vector.loader.DocumentLoader;
import ai.vector.loader.pojo.SplitConfig;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class MarkdownLoader implements DocumentLoader {
    @Override
    public List<List<FileChunkResponse.Document>> load(String path, SplitConfig splitConfig) {
        List<FileChunkResponse.Document> documents = mdLoad(path, splitConfig);
        List<List<FileChunkResponse.Document>> res = new ArrayList<>();
        if (documents != null && !documents.isEmpty()) {
            res.add(documents);
        }
        return res;
    }
    public List<FileChunkResponse.Document> mdLoad(String path, SplitConfig splitConfig) {
        File file = new File(path);
        String content = FileService.getString(file.getPath()).replaceAll("!\\[.*?\\]\\(.*?\\)", "");
        return MarkdownExtractorUtil.getChunkDocument(content, splitConfig.getChunkSizeForText());
    }
}

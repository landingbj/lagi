package ai.vector.loader.impl;

import ai.common.pojo.FileChunkResponse;
import ai.utils.PptUtil;
import ai.vector.loader.DocumentLoader;
import ai.vector.loader.pojo.SplitConfig;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
public class PptLoader implements DocumentLoader {
    @Override
    public List<List<FileChunkResponse.Document>> load(String path, SplitConfig splitConfig) {
        List<FileChunkResponse.Document> documents = pptLoad(path, splitConfig);
        List<List<FileChunkResponse.Document>> res = new ArrayList<>();
        if (documents != null && !documents.isEmpty()) {
            res.add(documents);
        }
        return res;
    }

    public List<FileChunkResponse.Document> pptLoad(String path, SplitConfig splitConfig) {
        File file = new File(path);
        try {
            return PptUtil.getChunkDocumentPpt(file, splitConfig.getChunkSizeForMixUp());
        } catch (Exception e) {
            log.error("load image file error", e);
        }
        return Collections.emptyList();
    }
}

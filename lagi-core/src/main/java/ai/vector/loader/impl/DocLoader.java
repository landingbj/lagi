package ai.vector.loader.impl;

import ai.common.pojo.FileChunkResponse;
import ai.utils.*;
import ai.utils.word.WordUtils;
import ai.vector.FileService;
import ai.vector.loader.DocumentLoader;
import ai.vector.loader.pojo.SplitConfig;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;

@Slf4j
public class DocLoader implements DocumentLoader {

    @Override
    public List<FileChunkResponse.Document> load(String path, SplitConfig splitConfig) {
        FileService fileService = new FileService();
        File file = new File(path);
        String content = null;
        String extString = file.getName().substring(file.getName().lastIndexOf("."));
        try {
            InputStream in = Files.newInputStream(file.toPath());
            content = WordUtils.getContentsByWord(in, extString).replaceAll("\\n+", "\n");;
            content = content!=null? FileService.removeDirectory(content):content;
        } catch (Exception e) {
            log.error("load doc file error", e);
        }
        if(content == null) {
            return Collections.emptyList();
        }
        if (QaExtractorUtil.extractQA(content, splitConfig.getCategory(), splitConfig.getExtra(), splitConfig.getChunkSizeForText())) {
            return Collections.emptyList();
        }
        if (ChapterExtractorUtil.isChapterDocument(content)) {
            return ChapterExtractorUtil.getChunkDocument(content, splitConfig.getChunkSizeForText());
        } else if (SectionExtractorUtil.isChapterDocument(content, splitConfig.getChunkSizeForText())) {
            return SectionExtractorUtil.getChunkDocument(content, splitConfig.getChunkSizeForText());
        } else if (OrdinanceExtractorUtil.isOrdinanceDocument(content)) {
            return OrdinanceExtractorUtil.getChunkDocument(content, splitConfig.getChunkSizeForText());
        }
        else {
            try {
                if (WordDocxUtils.checkImagesInWord(file)){
                    FileChunkResponse response = fileService.extractContent(file);
                    if (response != null && response.getStatus().equals("success")) {
                        return response.getData();
                    } else {
                        return FileService.splitContentChunks(splitConfig.getChunkSizeForText(), content);
                    }
                }else {
                    System.out.println("不包含图片类文档");
                    return fileService.splitContentChunks(splitConfig.getChunkSizeForText(), content);
                }
            } catch (Exception e) {
                log.error("load doc file error", e);
            }
        }
        return Collections.emptyList();
    }
}

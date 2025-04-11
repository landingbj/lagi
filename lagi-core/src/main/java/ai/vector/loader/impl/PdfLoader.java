package ai.vector.loader.impl;

import ai.common.pojo.FileChunkResponse;
import ai.common.pojo.Response;
import ai.utils.*;
import ai.utils.pdf.PdfUtil;
import ai.vector.FileService;
import ai.vector.loader.DocumentLoader;
import ai.vector.loader.pojo.SplitConfig;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;

@Slf4j
public class PdfLoader implements DocumentLoader {

    // checked
    @Override
    public List<FileChunkResponse.Document> load(String path, SplitConfig splitConfig) {
        File file = new File(path);
        FileService fileService = new FileService();
        try {
            String content = null;
            InputStream in = Files.newInputStream(file.toPath());
            content = PdfUtil.webPdfParse(in);
            if (content==null||content.trim().isEmpty()){
                System.out.println("扫描件");
                return FileService.getChunkDocumentScannedPDF(file,splitConfig.getChunkSizeForMixUp());
            }
            Response response = fileService.toMarkdown(file);
            if (response != null && response.getStatus().equals("success")){
                content = response.getData();
                content = content!=null?FileService.removeDirectory(content):content;
            }else {
                content = content
                        .replaceAll("(\r?\n){2,}", "\n")
                        .replaceAll("(?<=\r?\n)\\s*", "")
                        .replaceAll("(?<![.!?;:。！？；：\\s\\d])\r?\n", "");
                if (StrUtil.isBlank(content)) {
                    content = FileService.removeDirectory(content);
                } else {
                    return FileService.getChunkDocumentScannedPDF(file,splitConfig.getChunkSizeForMixUp());
                }
            }

            // qa 对
            if (QaExtractorUtil.extractQA(content, splitConfig.getCategory(), splitConfig.getExtra(), splitConfig.getChunkSizeForText())) {
                return Collections.emptyList();
            }

            if (ChapterExtractorUtil.isChapterDocument(content)) {
                return ChapterExtractorUtil.getChunkDocument(content, splitConfig.getChunkSizeForText());
            } else if (SectionExtractorUtil.isChapterDocument(content, splitConfig.getChunkSizeForText())) {
                return SectionExtractorUtil.getChunkDocument(content, splitConfig.getChunkSizeForText());
            } else if (OrdinanceExtractorUtil.isOrdinanceDocument(content)) {
                return OrdinanceExtractorUtil.getChunkDocument(content, splitConfig.getChunkSizeForText());
            }else {
                try {
                    if (WordDocxUtils.checkImagesInWord(file)){
                        FileChunkResponse response1 = fileService.extractContent(file);
                        if (response1 != null && response1.getStatus().equals("success")) {
                            return response1.getData();
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
            if(content!=null) {
                return FileService.splitContentChunks(splitConfig.getChunkSizeForMixUp(), content);
            }
        }catch (Exception e) {
            log.error("load pdf file error", e);
        }
        return Collections.emptyList();
    }
}

package ai.ocr;

import ai.common.utils.FileUtils;
import ai.common.utils.PdfUtils;
import ai.manager.OcrManager;
import ai.utils.LRUCache;
import ai.ocr.pojo.OcrProgress;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class OcrService {
    private static final String ocrCacheDir = OcrConfig.getOcrCacheDir();
    private static final LRUCache<String, OcrProgress> processedPageSizeCache = new LRUCache<>(1000);

    public String recognize(BufferedImage image) {
        for (IOcr adapter : OcrManager.getInstance().getAdapters()) {
            return adapter.recognize(image);
        }
        return null;
    }

    public List<String> recognize(File file) throws IOException {
        return recognize(null, file, -1, -1);
    }

    public List<List<String>> recognize(String taskId, List<File> fileList)  throws IOException {
        List<List<String>> result = new ArrayList<>();
        for (int i = 0; i < fileList.size(); i++) {
            File file = fileList.get(i);
            result.add(recognize(taskId, file, i, fileList.size()));
        }
        OcrProgress ocrProgress = processedPageSizeCache.get(taskId);
        ocrProgress.setProcessedFileSize(fileList.size());
        ocrProgress.setTotalFileSize(fileList.size());
        return result;
    }

    public List<String> recognize(String taskId, File file, int processedFileSize, int totalFileSize) throws IOException {
        List<BufferedImage> pageImages = PdfUtils.toImages(file);
        List<String> result = new ArrayList<>();

        String md5 = FileUtils.md5sum(file);
        String cacheDir = ocrCacheDir + "/" + md5;
        File cacheDirFile = new File(cacheDir);
        if (!cacheDirFile.exists() && OcrConfig.isOcrCacheEnable()) {
            cacheDirFile.mkdirs();
        }

        for (int i = 0; i < pageImages.size(); i++) {
            BufferedImage image = pageImages.get(i);
            int page = i + 1;
            String pageCacheFile = cacheDir + "/" + page + ".txt";
            String resultText;
            try {
                TimeUnit.MILLISECONDS.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            if (new File(pageCacheFile).exists() && OcrConfig.isOcrCacheEnable()) {
                resultText = FileUtils.readTextFromFile(pageCacheFile);
            } else {
                resultText = recognize(image);
                if (resultText != null && OcrConfig.isOcrCacheEnable()) {
                    FileUtils.writeTextToFile(pageCacheFile, resultText);
                }
            }
            result.add(resultText);

            if (taskId != null) {
                processedPageSizeCache.put(taskId, OcrProgress.builder()
                        .md5(md5)
                        .totalPageSize(pageImages.size())
                        .processedPageSize(page)
                        .processedFileSize(processedFileSize)
                        .totalFileSize(totalFileSize)
                        .build());
            }
        }
        return result;
    }

    public OcrProgress getOcrProgress(String taskId) {
        return processedPageSizeCache.get(taskId);
    }
}

package ai.ocr;

import ai.common.utils.FileUtils;
import ai.common.utils.PdfUtils;
import ai.manager.AIManager;
import ai.manager.DocOcrManager;
import ai.manager.OcrManager;
import ai.utils.LRUCache;
import ai.ocr.pojo.OcrProgress;
import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
public class OcrService {
    private static final String ocrCacheDir = OcrConfig.getOcrCacheDir();
    private static final LRUCache<String, OcrProgress> processedPageSizeCache = new LRUCache<>(1000);

    private final AIManager<IOcr> imageOrcManager;
    private final AIManager<IOcr> docOcrManager;

    public OcrService() {
        this.imageOrcManager = OcrManager.getInstance();
        this.docOcrManager = DocOcrManager.getInstance();
    }


    private List<IOcr> getAdapterByBackends(AIManager<IOcr> ocrAIManager) {
        return ocrAIManager.getAdapters();
    }

    private String ocr(BufferedImage image, List<String> languages, AIManager<IOcr> aiManager) {

        for (IOcr adapter : getAdapterByBackends(aiManager)) {
            return adapter.recognize(image, languages);
        }
        return null;
    }

    public List<String> image2Ocr(List<File> imageFileList) {
        return image2Ocr(imageFileList, null);
    }

    public List<String> image2Ocr(List<File> imageFileList, List<String> languages) {
        List<String> result = new ArrayList<>();
        for (File file : imageFileList) {
            if (file == null) {
                result.add(null);
                continue;
            }
            try {
                BufferedImage image = ImageIO.read(file);
                result.add(ocr(image, languages, imageOrcManager));
            } catch (IOException e) {
                log.error("read image error {}", e.getMessage());
                result.add(null);
            }
        }
        return result;
    }



    public List<String> doc2ocr(File file, List<String> languages) throws IOException, PdfPageSizeLimitException {
        return doc2ocr(null, file, -1, -1, languages);
    }


    public List<String> doc2ocr(String taskId, File file, int processedFileSize, int totalFileSize, List<String> languages) throws IOException, PdfPageSizeLimitException {
        List<BufferedImage> pageImages = PdfUtils.toImages(file);
        if (pageImages.size() > 200) {
            throw new PdfPageSizeLimitException();
        }
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
                resultText = ocr(image, languages, docOcrManager);
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

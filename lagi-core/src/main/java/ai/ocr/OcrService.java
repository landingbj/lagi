package ai.ocr;

import ai.common.utils.FileUtils;
import ai.common.utils.PdfUtils;
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

    public String recognize(BufferedImage image) {
        for (IOcr adapter : OcrManager.getInstance().getAdapters()) {
            return adapter.recognize(image);
        }
        return null;
    }

    public String recognize(BufferedImage image, List<String> languages) {
        for (IOcr adapter : OcrManager.getInstance().getAdapters()) {
            return adapter.recognize(image, languages);
        }
        return null;
    }

    public List<String> recognize(List<File> imageFileList) {
        return recognize(imageFileList, null);
    }

    public List<String> recognize(List<File> imageFileList, List<String> languages) {
        List<String> result = new ArrayList<>();
        for (File file : imageFileList) {
            if (file == null) {
                result.add(null);
                continue;
            }
            try {
                BufferedImage image = ImageIO.read(file);
                result.add(recognize(image, languages));
            } catch (IOException e) {
                log.error("read image error {}", e.getMessage());
                result.add(null);
            }
        }
        return result;
    }

    public List<String> recognizePdf(File file) throws IOException, PdfPageSizeLimitException {
        return recognizePdf(null, file, -1, -1, null);
    }

    public List<String> recognizePdf(File file, List<String> languages) throws IOException, PdfPageSizeLimitException {
        return recognizePdf(null, file, -1, -1, languages);
    }

    public List<List<String>> recognizePdf(String taskId, List<File> fileList) throws IOException, PdfPageSizeLimitException {
        return recognizePdf(null, fileList, null);
    }

    public List<List<String>> recognizePdf(String taskId, List<File> fileList, List<String> languages) throws IOException, PdfPageSizeLimitException {
        List<List<String>> result = new ArrayList<>();
        for (int i = 0; i < fileList.size(); i++) {
            File file = fileList.get(i);
            result.add(recognizePdf(taskId, file, i, fileList.size(), languages));
        }
        OcrProgress ocrProgress = processedPageSizeCache.get(taskId);
        ocrProgress.setProcessedFileSize(fileList.size());
        ocrProgress.setTotalFileSize(fileList.size());
        return result;
    }

    public List<String> recognizePdf(String taskId, File file, int processedFileSize, int totalFileSize, List<String> languages) throws IOException, PdfPageSizeLimitException {
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
                resultText = recognize(image, languages);
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

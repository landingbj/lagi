package ai.ocr;

import ai.common.utils.FileUtils;
import ai.common.utils.PdfUtils;
import ai.manager.OcrManager;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class OcrService {
    private static final String ocrCacheDir = OcrConfig.getOcrCacheDir();

    public String recognize(BufferedImage image) {
        for (IOcr adapter : OcrManager.getInstance().getAdapters()) {
            return adapter.recognize(image);
        }
        return null;
    }

    public List<String> recognize(File file) throws IOException {
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
            String pageCacheFile = cacheDir + "/" + (i + 1) + ".txt";
            String resultText;

            if (new File(pageCacheFile).exists() && OcrConfig.isOcrCacheEnable()) {
                resultText = FileUtils.readTextFromFile(pageCacheFile);
            } else {
                resultText = recognize(image);
                if (resultText != null && OcrConfig.isOcrCacheEnable()) {
                    FileUtils.writeTextToFile(pageCacheFile, resultText);
                }
            }
            result.add(resultText);
        }
        return result;
    }
}

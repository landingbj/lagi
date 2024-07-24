package ai.ocr;

import ai.common.pojo.Backend;
import ai.common.utils.FileUtils;

import java.util.List;

public class OcrConfig {
    private static String OCR_CACHE_DIR = FileUtils.getOsTempDir() + "/ocr-cache";
    private static boolean OCR_CACHE_ENABLE = false;

    public static void init(List<Backend> models) {
        if (models == null || models.isEmpty()) {
            return;
        }
        OCR_CACHE_ENABLE = models.stream().anyMatch(backend -> backend.getCacheEnable() != null && backend.getCacheEnable());
        OCR_CACHE_DIR = models.stream().filter(backend -> backend.getCacheDir() != null).findFirst().map(Backend::getCacheDir).orElse(OCR_CACHE_DIR);
    }

    public static String getOcrCacheDir() {
        return OCR_CACHE_DIR;
    }

    public static boolean isOcrCacheEnable() {
        return OCR_CACHE_ENABLE;
    }
}


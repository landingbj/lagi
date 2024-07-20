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

    private static final String ocrCacheDir = "D:/apps/cache/ocr-cache";

    static {
        File cacheDir = new File(ocrCacheDir);
        if (!cacheDir.exists()) {
            cacheDir.mkdirs();
        }
    }

    public String recognize(BufferedImage image) {
        for (IOcr adapter : OcrManager.getInstance().getAdapters()) {
            return adapter.recognize(image);
        }
        return null;
//        return "ocr.recognize(image)";
    }

    public List<String> recognize(File file) throws IOException {
        List<BufferedImage> pageImages = PdfUtils.toImages(file);
        List<String> result = new ArrayList<>();
        String md5 = FileUtils.md5sum(file);
        String cacheDir = ocrCacheDir + "/" + md5;
        File cacheDirFile = new File(cacheDir);
        if (!cacheDirFile.exists()) {
            cacheDirFile.mkdirs();
        }

        for (int i = 0; i < pageImages.size(); i++) {
            BufferedImage image = pageImages.get(i);
            String pageCacheFile = cacheDir + "/" + (i + 1) + ".txt";
            String resultText;

//            File outputfile = new File(cacheDir + "/" + (i + 1) + ".png");
//            ImageIO.write(image, "png", outputfile);

            if (new File(pageCacheFile).exists()) {
                resultText = FileUtils.readTextFromFile(pageCacheFile);
                System.out.println("read from cache: " + pageCacheFile);
//                resultText = ocr.toFormatedText(resultText);
//                FileUtils.writeTextToFile(pageCacheFile, resultText);
            } else {
                resultText = recognize(image);
                FileUtils.writeTextToFile(pageCacheFile, resultText);
            }
            result.add(resultText);
        }
//        System.out.println(result);
        return result;
    }
}

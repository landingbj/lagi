package ai.ocr;

import java.awt.image.BufferedImage;
import java.util.List;

public interface IOcr {
    String recognize(BufferedImage image);

    String recognize(BufferedImage image, List<String> languages);
}

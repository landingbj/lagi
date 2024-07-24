package ai.ocr;

import java.awt.image.BufferedImage;

public interface IOcr {
    String recognize(BufferedImage image);
}

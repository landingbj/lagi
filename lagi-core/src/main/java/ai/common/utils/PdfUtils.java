package ai.common.utils;


import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PdfUtils {
    /**
     * Converts a PDF file to a list of images.
     *
     * @param file the PDF file
     * @return the list of images
     * @throws IOException if an I/O error occurs
     */
    public static List<BufferedImage> toImages(File file) throws IOException {
        List<BufferedImage> images = new ArrayList<>();
        try (PDDocument document = PDDocument.load(file)) {
            PDFRenderer pdfRenderer = new PDFRenderer(document);
            for (int page = 0; page < document.getNumberOfPages(); ++page) {
                BufferedImage image = pdfRenderer.renderImageWithDPI(page, 120, ImageType.RGB);
                images.add(image);
            }
        }
        return images;
    }

    public static int getNumberOfPages(File file) {
        try (PDDocument document = PDDocument.load(file)) {
            return document.getNumberOfPages();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static int getNumberOfPages(List<File> fileList) {
        int total = 0;
        for (File file : fileList) {
            total += getNumberOfPages(file);
        }
        return total;
    }
}
package ai.utils;

import org.mozilla.universalchardet.UniversalDetector;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class EncodingDetector {
    /**
     * Detect file encoding using JUniversalChardet
     *
     * @param filePath path to the file
     * @return detected encoding name, null if unable to detect
     */
    public static String detectEncoding(String filePath) {
        File file = new File(filePath);

        try {
            // First, try to detect BOM
            String bomEncoding = detectBOM(file);
            if (bomEncoding != null) {
                return bomEncoding;
            }

            // Use UniversalDetector for encoding detection
            UniversalDetector detector = new UniversalDetector(null);

            try (FileInputStream fis = new FileInputStream(file);
                 BufferedInputStream bis = new BufferedInputStream(fis)) {

                byte[] buffer = new byte[4096];
                int bytesRead;

                // Feed data to detector
                while ((bytesRead = bis.read(buffer)) > 0 && !detector.isDone()) {
                    detector.handleData(buffer, 0, bytesRead);
                }

                // Signal end of data
                detector.dataEnd();

                // Get detected encoding
                String encoding = detector.getDetectedCharset();

                // Reset detector for potential reuse
                detector.reset();

                return encoding;
            }

        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
            return null;
        }
    }

    /**
     * Detect BOM (Byte Order Mark) at the beginning of file
     *
     * @param file the file to check
     * @return encoding if BOM is detected, null otherwise
     * @throws IOException if file reading fails
     */
    private static String detectBOM(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] bom = new byte[4];
            int bytesRead = fis.read(bom);

            if (bytesRead >= 3) {
                // UTF-8 BOM: EF BB BF
                if (bom[0] == (byte) 0xEF && bom[1] == (byte) 0xBB && bom[2] == (byte) 0xBF) {
                    return "UTF-8";
                }

                // UTF-16 BE BOM: FE FF
                if (bom[0] == (byte) 0xFE && bom[1] == (byte) 0xFF) {
                    return "UTF-16BE";
                }

                // UTF-16 LE BOM: FF FE
                if (bom[0] == (byte) 0xFF && bom[1] == (byte) 0xFE) {
                    return "UTF-16LE";
                }
            }

            if (bytesRead >= 4) {
                // UTF-32 BE BOM: 00 00 FE FF
                if (bom[0] == 0x00 && bom[1] == 0x00 &&
                        bom[2] == (byte) 0xFE && bom[3] == (byte) 0xFF) {
                    return "UTF-32BE";
                }

                // UTF-32 LE BOM: FF FE 00 00
                if (bom[0] == (byte) 0xFF && bom[1] == (byte) 0xFE &&
                        bom[2] == 0x00 && bom[3] == 0x00) {
                    return "UTF-32LE";
                }
            }
        }

        return null;
    }
}
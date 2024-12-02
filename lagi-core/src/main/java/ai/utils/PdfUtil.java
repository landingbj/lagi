package ai.utils;

import ai.common.pojo.TextBlock;
import ai.vector.FileService;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;


import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;


@Slf4j
public class PdfUtil {

    private static final Lock convertLock = new ReentrantLock();
    private static final String SOFFICE_PATH = "D:\\Tools\\LibreOffice\\program\\soffice.exe";
    private static final int ZOOM_SIZE = 1;
    private static final float DPI = 300;

    public static final LRUCache<String, String> pdfStrCache = new LRUCache<>(100);
    public static final LRUCache<String, List<TextBlock>> pdfTextBlockCache = new LRUCache<>(100);
    public static ReentrantLock lock = new ReentrantLock();

    public static String convertToPdf(String docPath, String pdfPath) {
        try {
            convertLock.lock();
            int exitCode = getExitCode(docPath, pdfPath);

            if (exitCode == 0) {
                System.out.println("Conversion successful.");
            } else {
                System.err.println("Conversion failed with exit code: " + exitCode);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            convertLock.unlock();
        }

        return pdfPath;
    }

    private static int getExitCode(String docPath, String pdfPath) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder(
                SOFFICE_PATH,
                "--headless",
                "--convert-to",
                "pdf",
                "--outdir",
                pdfPath,
                docPath
        );

        processBuilder.redirectErrorStream(true);

        Process process = processBuilder.start();
        int exitCode = process.waitFor();
        return exitCode;
    }

    public static List<List<TextBlock>> getAllWordsCoordinateByPath(String pdfPath, String searchWord) {
        List<List<TextBlock>>  data = new ArrayList<>();
        try {
            PDDocument document = PDDocument.load(new File(pdfPath));
            PDFTextExtractor extractor = new PDFTextExtractor(searchWord, true);
            PDFTextStripper pdfTextStripper = new PDFTextStripper();
            extractor.setSortByPosition(true);
            for (int i = 0; i < document.getNumberOfPages(); i++) {
                extractor.setStartPage(i + 1);
                extractor.setEndPage(i + 1);
                pdfTextStripper.setStartPage(i+1);
                pdfTextStripper.setEndPage(i+1);
                String key = pdfPath + "-" + i;
                String text = pdfStrCache.get(key);
                if(text == null) {
                    text =pdfTextStripper.getText(document).replaceAll("\\s+", "");
                    pdfStrCache.put(key, text);
                }
                extractor.setCursorPoint(0);
                extractor.setCurrentPageText(text);
                extractor.getText(document);
            }
            document.close();
            return extractor.getResults();
        } catch (IOException e) {
            log.error("Error reading PDF file: {}", e.getMessage());
        }
        return data;
    }

    private static List<List<Float>> convert(List<TextBlock> blocks) {
        return blocks.stream().map(block -> {
            float pageNo = (float) block.getPageNo();
            List<Float> list = new ArrayList<>();
            list.add(block.getX());
            list.add(block.getY());
            list.add(pageNo);
            return list;
        }).collect(Collectors.toList());
    }

    public static List<List<TextBlock>> searchFromCache(String path, String searchWord, boolean isAll) {
        List<List<TextBlock>> res = new ArrayList<>();
        List<TextBlock> textBlockFromCache = getTextBlockFromCache(path);
        StringBuilder read = new StringBuilder();
        List<Integer> counts = new ArrayList<>();
        for (int i = 0; i < textBlockFromCache.size(); i++) {
            String text = textBlockFromCache.get(i).getText();
            read.append(text);
            counts.add(text.length());
            if(read.toString().contains(searchWord)) {
                int countLen = 0;
                int j = counts.size() - 1;
                int m = 0;
                for (; j >= 0; j--) {
                    countLen += counts.get(j);
                    m ++;
                    if(countLen >= searchWord.length()) {
                        break;
                    }
                }
                List<TextBlock> textBlocks = textBlockFromCache.subList(i + 1 - m, i + 1);
                res.add(textBlocks);
                counts.clear();
                read = new StringBuilder();
                if(!isAll) {
                    break;
                }
            }
        }
        return res;
    }

    public static List<TextBlock> getTextBlockFromCache(String pdfPath){
        List<TextBlock>  textBlocks = new ArrayList<>();
        if(pdfTextBlockCache.containsKey(pdfPath)) {
            return pdfTextBlockCache.get(pdfPath);
        }
        try {
            boolean b = lock.tryLock(30, TimeUnit.SECONDS);
            if(!b) {
                throw new RuntimeException("lock timeout");
            }
            if(pdfTextBlockCache.containsKey(pdfPath)) {
                return pdfTextBlockCache.get(pdfPath);
            }
            FileService fileService = new FileService();
            try {
                File file = new File(pdfPath);
                PDDocument document = PDDocument.load(file);
                String fileContent = fileService.getFileContent(file);
                PDFTextBlockExtractor pdfTextBlockExtractor = new PDFTextBlockExtractor(fileContent);
                pdfTextBlockExtractor.setSortByPosition(true);
                for (int i = 0; i < document.getNumberOfPages(); i++) {
                    pdfTextBlockExtractor.setStartPage(i + 1);
                    pdfTextBlockExtractor.setEndPage(i + 1);
                    pdfTextBlockExtractor.getText(document);
                }
                textBlocks = pdfTextBlockExtractor.getTextBlocks();
                document.close();
                pdfTextBlockCache.put(pdfPath, textBlocks);
            } catch (IOException e) {
                log.error("get text block from cache error {}", pdfPath, e);
            }

        } catch (InterruptedException e) {
            log.error("get lock error {}", pdfPath, e);
        } finally {
            lock.unlock();
        }
        return textBlocks;
    }

    public static String getCroppedPageImage(String pdfPath, String pageDir, int pageIndex, int x0, int y0, int x1, int y1) throws IOException {
        // 加载PDF文档
        try (PDDocument document = PDDocument.load(new File(pdfPath))) {

            // 创建PDF渲染器
            PDFRenderer renderer = new PDFRenderer(document);

            // 渲染页面为图像
            renderer.renderImageWithDPI(pageIndex, 105);
            BufferedImage fullPageImage = renderer.renderImage(pageIndex, ZOOM_SIZE);
            int width = fullPageImage.getWidth();
            int height = fullPageImage.getHeight();
            x0 = Math.max(x0, 0);
            y0 = Math.max(y0, 0);
            x1 = Math.min(width, x1);
            y1 = Math.min(height, y1);
//            // 计算裁剪区域
            int croppedWidth = (x1 - x0) * ZOOM_SIZE;
            int croppedHeight = (y1 - y0) * ZOOM_SIZE;
            int croppedX = x0 * ZOOM_SIZE;
            int croppedY = y0 * ZOOM_SIZE;

//            // 计算裁剪区域
//            int croppedWidth = (x1 - x0) ;
//            int croppedHeight = (y1 - y0) ;
//            int croppedX = x0 ;
//            int croppedY = y0 ;
            String pageImagePath = pageDir + "/" + (pageIndex + 1) + "_" + x0 + "_" + y0 + "_" + x1 + "_" + y1 + ".png";
            //   System.out.println(pageImagePath);
            // 裁剪图像
            BufferedImage croppedImage = fullPageImage.getSubimage(croppedX, croppedY, croppedWidth, croppedHeight);
            System.out.println(pageImagePath + "    finished");
            // croppedImage = pageImagePath.replace("\\", "/");
            // 保存图像
            File pageFile = new File(pageImagePath);
            // String path = pageFile.getAbsolutePath();
            ImageIO.write(croppedImage, "png", pageFile);
            return pageFile.getAbsolutePath();
        }
    }
}

package ai.sevice;

import ai.common.pojo.Configuration;
import ai.dto.BdBlock;
import ai.dto.DxDiagnosis;
import ai.ocr.OcrService;
import ai.ocr.pojo.AlibabaOcrDocument;
import ai.utils.HttpUtil;
import ai.utils.ImageUtil;
import ai.utils.LRUCache;
import ai.utils.LagiGlobal;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ImageBlockService {
    private static final Map<String, String> ERROR_CORRECTION_MAP;
    private static final Map<String, DxDiagnosis> DX_DIAGNOSIS_MAP;
    private static final LRUCache<String, List<DxDiagnosis>> CACHE = new LRUCache<>(1000);

    static {
        ERROR_CORRECTION_MAP = new ConcurrentHashMap<>(readErrorCorrection());
        DX_DIAGNOSIS_MAP = readDiagnosisDesc();
    }

    private static final Pattern ID_PATTERN = Pattern.compile("\\bD[a-zA-Z0-9]{1,3}\\d{2,4}\\b");

    private final OcrService ocrService = new OcrService();

    private static final Gson gson = new Gson();

    private String getImageHash(BufferedImage image) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");

            int width = image.getWidth();
            int height = image.getHeight();
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int rgb = image.getRGB(x, y);
                    md.update((byte) (rgb & 0xFF));
                    md.update((byte) ((rgb >> 8) & 0xFF));
                    md.update((byte) ((rgb >> 16) & 0xFF));
                    md.update((byte) ((rgb >> 24) & 0xFF));
                }
            }
            byte[] digest = md.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 algorithm not available", e);
        }
    }

    private List<String> getImageOcr(List<BufferedImage> images) {
        List<String> ocrResults = new ArrayList<>();
        for (BufferedImage image : images) {
            String ocrResult = ocrService.recognizeImage(image);
            if (ocrResult != null && !ocrResult.isEmpty()) {
                ocrResults.add(ocrResult);
            }
        }
        return ocrResults;
    }

    public String getAnalyzeImageResult(String imagePath) throws IOException, InterruptedException {
        List<DxDiagnosis> diagnoses = analyzeImage(imagePath);
        StringBuilder sb = new StringBuilder();
        for (DxDiagnosis diagnosis : diagnoses) {
            sb.append(diagnosis.getId()).append(" ")
                    .append(diagnosis.getShortDesc()).append("\n")
                    .append(diagnosis.getDetailDesc()).append("\n\n");
        }
        return sb.toString();
    }

    public List<DxDiagnosis> analyzeImage(String imagePath) throws IOException, InterruptedException {
        File inputFile = new File(imagePath);
        BufferedImage inputImage = ImageIO.read(inputFile);

        String md5Hash = getImageHash(inputImage);
        List<DxDiagnosis> diagnoses = CACHE.get(md5Hash);
        if (diagnoses != null) {
            Thread.sleep(3 * 1000);
            return diagnoses;
        }

        BufferedImage outputImage = ImageUtil.keepRedPart(inputImage);
        outputImage = ImageUtil.removeSmallRedAreas(outputImage, 5);

        List<Rectangle> rectangles = ImageUtil.getRedBoundingBoxes(outputImage, 20);

        List<BufferedImage> partImageList = ImageUtil.cropImageByRect(outputImage, rectangles);
        List<BufferedImage> expandedImageList = new ArrayList<>();

        for (int i = 0; i < partImageList.size(); i++) {
            BufferedImage partImage = partImageList.get(i);
            BufferedImage expandedImage = ImageUtil.expandImage(partImage, 22, 5);
            expandedImageList.add(expandedImage);
        }
//        List<String> ocrResults = getImageOcr(expandedImageList);
        List<String> ocrResults = readAllJsonFiles("E:\\Desktop\\络明芯规则\\bd_1_ocr");
        List<DxDiagnosis> results = parseOcrResults(ocrResults);
        if (!results.isEmpty()) {
            CACHE.put(md5Hash, results);
        }
        return parseOcrResults(ocrResults);
    }

    private List<DxDiagnosis> parseOcrResults(List<String> ocrResults) {
        List<DxDiagnosis> diagnoses = new ArrayList<>();
        for (String ocrResult : ocrResults) {
            AlibabaOcrDocument ocrDocument = gson.fromJson(ocrResult, AlibabaOcrDocument.class);
            List<DxDiagnosis> dxDiagnosis = parseOcrResult(ocrDocument);
            diagnoses.addAll(dxDiagnosis);
        }
        diagnoses.sort(Comparator.comparing(DxDiagnosis::getId));
        return diagnoses;
    }

    private List<DxDiagnosis> parseOcrResult(AlibabaOcrDocument ocrDocument) {
        List<DxDiagnosis> result = new ArrayList<>();
        if (ocrDocument.getPrism_rowsInfo() == null) {
            return result;
        }
        StringBuilder tmpStr = new StringBuilder();
        for (AlibabaOcrDocument.PrismRowInfo rowInfo : ocrDocument.getPrism_rowsInfo()) {
            String str = rowInfo.getWord();
            System.out.println(str);
        }
        return result;
    }

    private void correctDxDiagnosis(DxDiagnosis diagnosis) {
        String id = diagnosis.getId();
        String shortDesc = diagnosis.getShortDesc();
        for (Map.Entry<String, String> entry : ERROR_CORRECTION_MAP.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            id = id.replace(key, value);
            diagnosis.setId(id);
        }
        DxDiagnosis tmpDxDiagnosis = getDxDiagnosis(shortDesc.toLowerCase());
        diagnosis.setShortDesc(tmpDxDiagnosis.getShortDesc());
        diagnosis.setDetailDesc(tmpDxDiagnosis.getDetailDesc());
    }

    private DxDiagnosis getDxDiagnosis(String shortDesc) {
        Set<String> keys = DX_DIAGNOSIS_MAP.keySet();
        double maxRatio = -1;
        String bestKey = "";
        for (String key : keys) {
            double lcsRatio = lcsRatio(shortDesc, key);
            if (lcsRatio > maxRatio) {
                maxRatio = lcsRatio;
                bestKey = key;
            }
        }
        return DX_DIAGNOSIS_MAP.get(bestKey);
    }

    private double lcsRatio(String text1, String text2) {
        return (double) lcsLength(text1, text2) / Math.max(text1.length(), text2.length());
    }

    private int lcsLength(String text1, String text2) {
        int m = text1.length();
        int n = text2.length();

        int[][] dp = new int[m + 1][n + 1];
        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                if (text1.charAt(i - 1) == text2.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1] + 1;
                } else {
                    dp[i][j] = Math.max(dp[i - 1][j], dp[i][j - 1]);
                }
            }
        }

        return dp[m][n];
    }


    private String extractId(String text) {
        Matcher matcher = ID_PATTERN.matcher(text);
        if (matcher.find()) {
            return matcher.group();
        }
        return null;
    }

    private static List<String> readAllJsonFiles(String folderPath) throws IOException {
        List<String> jsonList = new ArrayList<>();
        File folder = new File(folderPath);
        if (!folder.isDirectory()) {
            throw new IllegalArgumentException("Path is not a directory: " + folderPath);
        }
        File[] files = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".json"));
        if (files == null) return jsonList;
        for (File file : files) {
            String content = new String(Files.readAllBytes(file.toPath()));
            jsonList.add(content);
        }
        return jsonList;
    }

    private static void saveJsonToFile(String json, String filePath) {
        try (FileWriter fileWriter = new FileWriter(filePath)) {
            fileWriter.write(json);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Map<String, String> readErrorCorrection() {
        String jsonPath = "/error-correction.json";
        String content = "{}";

        try (InputStream in = ImageBlockService.class.getResourceAsStream(jsonPath)) {
            content = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8)).lines().collect(Collectors.joining("\n"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        Type type = new TypeToken<Map<String, String>>() {
        }.getType();
        Map<String, String> map = new Gson().fromJson(content, type);
        return map;
    }

    private static Map<String, DxDiagnosis> readDiagnosisDesc() {
        String jsonPath = "/dx-diagnosis.json";
        String content = "{}";

        try (InputStream in = ImageBlockService.class.getResourceAsStream(jsonPath)) {
            content = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8)).lines().collect(Collectors.joining("\n"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        Type type = new TypeToken<List<DxDiagnosis>>() {
        }.getType();
        List<DxDiagnosis> list = new Gson().fromJson(content, type);
        Map<String, DxDiagnosis> map = new ConcurrentHashMap<>();
        for (DxDiagnosis diagnosis : list) {
            map.put(diagnosis.getShortDesc().toLowerCase(), diagnosis);
        }
        return map;
    }

    private List<BdBlock> getBlocksFromImage(String imagePath) {
        List<BdBlock> result = new ArrayList<>();
        String BLOCKS_URL = "http://0.0.0.0:8123/getBlocks";
        String filePramName = "file";
        Map<String, String> formParmMap = new HashMap<>();
        List<File> fileList = new ArrayList<>();
        fileList.add(new File(imagePath));
        Map<String, String> headers = new HashMap<>();
        String returnStr = HttpUtil.multipartUpload(BLOCKS_URL, filePramName, fileList, formParmMap, headers);
        System.out.println("Response: " + returnStr);
        if (returnStr == null || returnStr.isEmpty()) {
            return result;
        }
        Type type = new TypeToken<List<BdBlock>>() {
        }.getType();
        result = new Gson().fromJson(returnStr, type);
        return result;
    }

    private List<Rectangle> getBdRectFromImage(String imagePath) {
        List<BdBlock> bdBlocks = getBlocksFromImage(imagePath);
        List<Rectangle> rectangles = new ArrayList<>();
        for (BdBlock bdBlock : bdBlocks) {
            int x = bdBlock.getCircle().getX() - bdBlock.getCircle().getRadius();
            int y = bdBlock.getCircle().getY() - bdBlock.getCircle().getRadius();
            int width = bdBlock.getRectangle().getX1() - x;
            int height = bdBlock.getRectangle().getY1() - y;
            rectangles.add(new Rectangle(x, y, width, height));
            System.out.println("Circle: " + bdBlock.getCircle());
            System.out.println("Rectangle: " + bdBlock.getRectangle());
        }
        return rectangles;
    }

    private List<Rectangle> getBdIdFromImage(String imagePath) {
        List<BdBlock> bdBlocks = getBlocksFromImage(imagePath);
        List<Rectangle> rectangles = new ArrayList<>();
        for (BdBlock bdBlock : bdBlocks) {
            int innerRadius = bdBlock.getCircle().getRadius() ;
            int x = bdBlock.getCircle().getX() - innerRadius;
            int y = bdBlock.getCircle().getY() - innerRadius;
            int width = innerRadius * 2;
            int height = innerRadius * 2;
            rectangles.add(new Rectangle(x, y, width, height));
            System.out.println("Circle: " + bdBlock.getCircle());
            System.out.println("Rectangle: " + bdBlock.getRectangle());
        }
        return rectangles;
    }

    public static void main(String[] args) throws IOException {
        LagiGlobal.getConfig();
        ImageBlockService imageBlockService = new ImageBlockService();

        String imagePath = "E:\\Desktop\\络明芯规则\\bd_2.png";
//        List<Rectangle> rectangles = imageBlockService.getBdRectFromImage(imagePath);
        List<Rectangle> rectangles = imageBlockService.getBdIdFromImage(imagePath);

        for (Rectangle rectangle : rectangles) {
            System.out.println(rectangle);
        }

        BufferedImage inputImage = ImageIO.read(new File(imagePath));
        List<BufferedImage> partImageList = ImageUtil.cropImageByRect(inputImage, rectangles);

        BufferedImage idMergedImage = ImageUtil.mergeImages(partImageList, 10, 10, 10);
//        BufferedImage binIdMergedImage = ImageUtil.binaryImageOptimized(idMergedImage, 120);


        ImageIO.write(idMergedImage, "png", new File("id.png"));

        for (int i = 0; i < partImageList.size(); i++) {
            BufferedImage partImage = partImageList.get(i);
            String partImagePath = "part_" + (i + 1) + ".png";
            partImage = ImageUtil.expandImage(partImage, 10, 10);
            ImageIO.write(partImage, "png", new File(partImagePath));
            System.out.println("Part image saved: " + partImagePath);
        }

//        List<String> ocrResults = imageBlockService.getImageOcr(partImageList);
        List<String> ocrResults = readAllJsonFiles("E:\\Desktop\\络明芯规则\\bd_1_ocr_block");
        for (int i = 0; i < ocrResults.size(); i++) {
            System.out.println("OCR Result " + (i + 1) + ": " + ocrResults.get(i));
            String ocrResult = ocrResults.get(i);
            AlibabaOcrDocument ocrDocument = gson.fromJson(ocrResult, AlibabaOcrDocument.class);
            saveJsonToFile(ocrResults.get(i), (i + 1) + ".json");
            imageBlockService.parseOcrResult(ocrDocument);
            System.out.println();
        }

//        try {
//            String result = imageBlockService.getAnalyzeImageResult(imagePath);
//            System.out.println(result);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }
}

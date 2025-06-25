package ai.sevice;

import ai.dto.BdBlock;
import ai.dto.BlockDesc;
import ai.llm.service.CompletionsService;
import ai.openai.pojo.ChatCompletionRequest;
import ai.openai.pojo.ChatCompletionResult;
import ai.openai.pojo.ChatMessage;
import ai.openai.pojo.MultiModalContent;
import ai.utils.Base64Util;
import ai.utils.HttpUtil;
import ai.utils.ImageUtil;
import ai.utils.LagiGlobal;
import ai.utils.qa.ChatCompletionUtil;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ImageBlockService {
    private static final Map<String, BlockDesc> BLOCK_DESC_MAP;
    private static final ObjectMapper mapper;
    private static final String ID_PROMPT = "提取图片中的数字";
    private static final String BLOCK_PROMPT = "请从这张图片中提取所有可见的英文文字。图片中的文字位置分散，请确保逐行识别并输出每一段文字，每一行作为一个独立的字符串输出。保持原有的阅读顺序（从上到下、从左到右），无需去除标点符号或进行翻译。";
    private final CompletionsService completionsService = new CompletionsService();

    static {
        BLOCK_DESC_MAP = readBlockDesc();
        mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    private static Map<String, BlockDesc> readBlockDesc() {
        String jsonPath = "/block-desc.json";
        String content = "{}";

        try (InputStream in = ImageBlockService.class.getResourceAsStream(jsonPath)) {
            content = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8)).lines().collect(Collectors.joining("\n"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        Type type = new TypeToken<List<BlockDesc>>() {
        }.getType();
        List<BlockDesc> list = new Gson().fromJson(content, type);
        Map<String, BlockDesc> map = new ConcurrentHashMap<>();
        for (BlockDesc blockDesc : list) {
            map.put(blockDesc.getBlock().toLowerCase(), blockDesc);
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
        if (returnStr == null || returnStr.isEmpty()) {
            return result;
        }
        Type type = new TypeToken<List<BdBlock>>() {
        }.getType();
        result = new Gson().fromJson(returnStr, type);
        return result;
    }


    private Rectangle getBdIdRect(BdBlock bdBlock) {
        int innerRadius = bdBlock.getCircle().getRadius() + 1;
        int x = bdBlock.getCircle().getX() - innerRadius;
        int y = bdBlock.getCircle().getY() - innerRadius;
        int width = innerRadius * 2;
        int height = innerRadius * 2;
        return new Rectangle(x, y, width, height);
    }

    private Rectangle getBlockRect(BdBlock bdBlock) {
        int x0 = bdBlock.getCircle().getX();
        int y0 = bdBlock.getCircle().getY();
        int x1 = bdBlock.getRectangle().getX1();
        int y1 = bdBlock.getRectangle().getY1();
        int width = x1 - x0;
        int height = y1 - y0;
        return new Rectangle(x0, y0, width, height);
    }


    public List<BlockDesc> analyzeBdImage(String imagePath) throws IOException {
        List<BdBlock> bdBlocks = getBlocksFromImage(imagePath);
        List<BlockDesc> blockDescList = new ArrayList<>();
        for (int i = 0; i < bdBlocks.size(); i++) {
            BdBlock bdBlock = bdBlocks.get(i);
            BlockDesc blockDesc = analyzeBdImage(imagePath, bdBlock, i);
            blockDescList.add(blockDesc);
        }
        blockDescList.sort(Comparator.comparing(BlockDesc::getId));
        return blockDescList;
    }

    private BlockDesc analyzeBdImage(String imagePath, BdBlock bdBlock, int i) throws IOException {
//        if (i != 5) {
//            return null;
//        }

        BufferedImage inputImage = ImageIO.read(new File(imagePath));
        BufferedImage idImage = getBdIdImages(inputImage, bdBlock);
        BufferedImage blockImage = getBlockImage(inputImage, bdBlock);

//        ImageIO.write(idImage, "png", new File(i + "_id.png"));
//        ImageIO.write(blockImage, "png", new File(i + "_block.png"));

        String id = extractId(idImage);
        BlockDesc blockDesc = extractBlockText(blockImage);
        if (id == null || id.isEmpty() || blockDesc == null || blockDesc.getBlock() == null || blockDesc.getBlock().isEmpty()) {
            return null;
        }
        blockDesc.setId(Integer.parseInt(id));
        blockDesc.setRectangle(bdBlock.getRectangle());
        return blockDesc;
    }

    private BufferedImage getBlockImage(BufferedImage inputImage, BdBlock bdBlock) throws IOException {
        Rectangle rectangle = getBlockRect(bdBlock);
        BufferedImage result = ImageUtil.cropImageByRect(inputImage, rectangle);
        if (result == null) {
            return null;
        }
        result = ImageUtil.expandImage(result, 10, 10);
        return result;
    }

    private BufferedImage getBdIdImages(BufferedImage inputImage, BdBlock bdBlock) throws IOException {
        Rectangle rectangle = getBdIdRect(bdBlock);
        BufferedImage result = ImageUtil.cropImageByRect(inputImage, rectangle);
        if (result == null) {
            return null;
        }
        result = ImageUtil.expandImage(result, 10, 10);
        return result;
    }

    private String extractId(BufferedImage image) throws IOException {
        ChatCompletionRequest chatCompletionRequest = getChatCompletionRequest(ID_PROMPT, image);
        ChatCompletionResult chatCompletionResult = completionsService.completions(chatCompletionRequest);
        String returnStr = ChatCompletionUtil.getFirstAnswer(chatCompletionResult);
        List<String> idList = extractNumbers(returnStr);
        String result = null;
        if (!idList.isEmpty()) {
            result = idList.get(0);
        }
        return result;
    }

    private BlockDesc extractBlockText(BufferedImage image) throws IOException {
        ChatCompletionRequest chatCompletionRequest = getChatCompletionRequest(BLOCK_PROMPT, image);
        ChatCompletionResult chatCompletionResult = completionsService.completions(chatCompletionRequest);
        String returnStr = ChatCompletionUtil.getFirstAnswer(chatCompletionResult);
        List<String> lines = Arrays.asList(returnStr.split("\n"));
        BlockDesc result = getBlockDesc(lines);
        return result;
    }

    public static String cleanVlResult(String input) {
        return input.toLowerCase().replaceAll("\\(.*?\\)|（.*?）", "").trim();
    }

    private BlockDesc getBlockDesc(List<String> blockNameList) {
        Set<String> keys = BLOCK_DESC_MAP.keySet();
        double maxRatio = -1;
        String bestKey = "";
        for (String blockName : blockNameList) {
//            System.out.println(blockName);
            blockName = cleanVlResult(blockName);
            for (String key : keys) {
                double lcsRatio = lcsRatio(blockName, key);
//                System.out.println(blockName + " " + key + " " + lcsRatio);
                if (lcsRatio > maxRatio) {
                    maxRatio = lcsRatio;
                    bestKey = key;
                }
            }
        }
        return BLOCK_DESC_MAP.get(bestKey);
    }

    private List<String> extractNumbers(String input) {
        List<String> numbers = new ArrayList<>();
        Pattern pattern = Pattern.compile("\\d+");  // 匹配一个或多个数字
        Matcher matcher = pattern.matcher(input);
        while (matcher.find()) {
            numbers.add(matcher.group());
        }
        return numbers;
    }

    private ChatCompletionRequest getChatCompletionRequest(String prompt, BufferedImage image) throws IOException {
        ChatCompletionRequest chatCompletionRequest = new ChatCompletionRequest();
        chatCompletionRequest.setTemperature(0);
        chatCompletionRequest.setStream(false);
        chatCompletionRequest.setModel("qwen-vl-max");
        chatCompletionRequest.setMax_tokens(1024);
        List<ChatMessage> messages = new ArrayList<>();

        String base64Image = Base64Util.convertImageToBase64(image, "png");
        List<MultiModalContent> contentList = getMultiModalContents(prompt, base64Image);
        String json = mapper.writeValueAsString(contentList);

        ChatMessage message = new ChatMessage();
        message.setRole(LagiGlobal.LLM_ROLE_USER);
        message.setContent(json);
        messages.add(message);
        chatCompletionRequest.setMessages(messages);
        return chatCompletionRequest;
    }

    private List<MultiModalContent> getMultiModalContents(String prompt, String base64Image) {
        List<MultiModalContent> contentList = new ArrayList<>();

        MultiModalContent textContent = new MultiModalContent();
        textContent.setType("text");
        textContent.setText(prompt);
        contentList.add(textContent);

        MultiModalContent imageContent = new MultiModalContent();
        imageContent.setType("image_url");
        MultiModalContent.ImageUrl imageUrl = new MultiModalContent.ImageUrl();
        imageUrl.setUrl(base64Image);
        imageContent.setImageUrl(imageUrl);
        contentList.add(imageContent);
        return contentList;
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

    public static void main(String[] args) throws IOException {
        LagiGlobal.getConfig();
        ImageBlockService imageBlockService = new ImageBlockService();

        String imagePath = "E:\\Desktop\\络明芯规则\\bd_1.png";
//        List<Rectangle> rectangles = imageBlockService.getBdRectFromImage(imagePath);
        List<BlockDesc> blockDescList = imageBlockService.analyzeBdImage(imagePath);
        for (BlockDesc blockDesc : blockDescList) {
            System.out.println(blockDesc.getId() + " " + blockDesc.getBlock() + " " + blockDesc.getRectangle());
        }
    }
}

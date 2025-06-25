package ai.servlet.api;

import ai.common.pojo.IndexSearchData;
import ai.common.pojo.Response;
import ai.dto.BlockDesc;
import ai.dto.BlockItem;
import ai.llm.service.CompletionsService;
import ai.openai.pojo.ChatCompletionRequest;
import ai.openai.pojo.ChatCompletionResult;
import ai.openai.pojo.ChatMessage;
import ai.servlet.BaseServlet;
import ai.sevice.ImageBlockService;
import ai.utils.MigrateGlobal;
import ai.vector.FileService;
import ai.vector.VectorStoreService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ImageAnalysisServlet extends BaseServlet {
    private static final String UPLOAD_DIR = "/upload";
    private static final Logger logger = LoggerFactory.getLogger(ImageAnalysisServlet.class);
    private final FileService fileService = new FileService();
    private final CompletionsService completionsService = new CompletionsService();
    private final Gson gson = new Gson();
    private final VectorStoreService vectorStoreService = new VectorStoreService();
    private final ImageBlockService imageBlockService = new ImageBlockService();

    // Background information constant remains unchanged
    private static final String BACKGROUND_INFO = "Bias\n" +
            "        To provide the basic voltage reference and current bias for analog block.\n" +
            "        Regulator\n" +
            "        To provide kinds of power for analog and digital block.\n" +
            "        e-Fuse\n" +
            "        Trimming for bandgap voltage.\n" +
            "        Digital\n" +
            "        Through decode command from MCU, output a PWM signal with programmable frequency and duty cycle. Also report fault when some error happens.\n" +
            "        Communication\n" +
            "        Communication block to receive UART or SPI data and transmit data to the related Register.\n" +
            "        Bypass switch\n" +
            "        Though PWM signal to control bypass switch on/off to dimming the parallel LED. Also feedback some information of bypass switch.\n" +
            "        FaultB flag\n" +
            "        Fault report output when error happens.\n" +
            "        Auxiliary block\n" +
            "        Read EEPROM data for host and synchronization PWM dimming for serial application.\n" +
            "        ADC\n" +
            "        Convert some analog input to digital data.\n" +
            "        Clock\n" +
            "        Output 3 types of clock signal: 1. CLK_SYS=32MHz; 2.CLK_PWM=32MHz spread spectrum optional; 3. 8MHz.\n" +
            "        Lumi-Bus transceiver\n" +
            "        Receive diff signal DH/DL to UART signal and transmitt UART signal to diff signal.\n" +
            "        reset circuit\n" +
            "        Power-on-reset (POR), internal reset condition or external reset command to set MCU to initial state.\n" +
            "        General purpose I/O (GPIO)\n" +
            "        General purpose I/O multiplexer with IOPAD including input/output buffers and internal pull up/down resistors.\n" +
            "        8051 Core\n" +
            "        1-Cycle 8051 CPU core with interrupt controller.\n" +
            "        FLASH Controller\n" +
            "        The flash controller connects the CPU to the on-chip embedded FLASH memory.\n" +
            "        SRAM with SRAM controller\n" +
            "        SRAM macro of 2K x 16 for data storage. The SRAM controller include the ECC encode/decode and error detection/correction for CPU access.\n" +
            "        LIN\n" +
            "        LIN-capable 16550-like EUART2 is an enhanced UART controller (EUART) with separate transmit and receive FIFO.\n" +
            "        I2C\n" +
            "        One master controller and one slave controller.\n" +
            "        SPI\n" +
            "        Serial Peripheral Interface (SPI) is an enhanced synchronous serial hardware which is compatible with most SPI specifications. It also includes separate transmit and receive FIFO.\n" +
            "        EUART1\n" +
            "        An enhanced UART controller (EUART) with separate transmit and receive FIFO.\n" +
            "        TCC\n" +
            "        Timer/Compare/Capture unit is based on a 16-bit counter with pre-scalable system clock as counting clock.\n" +
            "        TIMER\n" +
            "        Timer 0-4 are 16-bit timers/counters. Timer 5 is a 24-Bit timer which has selectable clock sources and can serve as wakeup source from stop/sleep modes.\n" +
            "        Low Supply Detector\n" +
            "        Low supply detector with control logic.\n" +
            "        IOSC\n" +
            "        16MHz/32MHz system clock +/- 2% accuracy.\n" +
            "        WDT\n" +
            "        Watchdog timers that can be used by a system supervisor or as an event timer. WDT3 is clocked by an independent SIOSC 128KHz clock.\n" +
            "        SIOSC\n" +
            "        128KHz slow clock for sleep mode and for WDT.\n" +
            "        ACMP\n" +
            "        Four analog comparators with programmable threshold and inputs through GPIO.\n" +
            "        PWM\n" +
            "        Programmable 6 channels 12/10/8 bit PWM duty with center-aligned outputs.\n" +
            "        Melody Controller\n" +
            "        A simple tone melody generator with multi-octave note capability.\n" +
            "        QED\n" +
            "        Quadrature encoder has three external inputs through GPIO multi-functions typically used for motor position detection.\n" +
            "        TOUCH KEY Controller\n" +
            "        Detection of minute capacitance change of finger touch action typically used for touch key HMI interface.\n" +
            "        CRC Accelerator\n" +
            "        Hardware CRC accelerator.\n" +
            "        Fail-safe\n" +
            "        Automatically enter a safe state in case of system failure to minimize risks.\n" +
            "        Fault report\n" +
            "        Record and notify the system of any errors or abnormal situations that occur.\n" +
            "        current setting\n" +
            "        The current configured parameters or operating values in the device or system.\n" +
            "        output channel\n" +
            "        The physical or logical path of system output signals or data.";

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        logger.info("Received POST request: {}", req.getRequestURI());
        req.setCharacterEncoding("UTF-8");
        resp.setHeader("Content-Type", "application/json;charset=utf-8");
        String url = req.getRequestURI();
        String method = url.substring(url.lastIndexOf("/") + 1);
        logger.debug("Extracted method: {}", method);

        if (method.equals("analyzeImage")) {
            this.analyzeImage(req, resp);
        } else {
            logger.warn("Unsupported method: {}", method);
            Response response = Response.builder().status("failed").msg("Unsupported method").build();
            responsePrint(resp, gson.toJson(response)); // Uses inherited method from BaseServlet
        }
    }

    private void analyzeImage(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        logger.info("Starting image analysis");
        String msg = null;
        List<File> files = new ArrayList<>();

        try {
            logger.debug("Attempting to get uploaded files");
            files = getUploadFile(req, MigrateGlobal.IMAGE_FILE_SIZE_LIMIT, MigrateGlobal.IMAGE_FILE_SIZE_LIMIT, UPLOAD_DIR);
            logger.info("Number of uploaded files: {}", files.size());
        } catch (Exception e) {
            msg = "Error parsing uploaded file";
            logger.error("Failed to parse uploaded file", e);
        }

        if (!files.isEmpty()) {
            try {
                File imageFile = files.get(0);
                logger.info("Processing image file: {}", imageFile.getName());
                String formattedOutput = analyzeImage(imageFile.getAbsolutePath());
                if (formattedOutput.trim().isEmpty()) {
                    throw new RuntimeException("Image processing failed: Empty response from endpoint");
                }
                logger.info("Image analysis result received: {}", formattedOutput);
                Response response = Response.builder()
                        .status("success")
                        .data(formattedOutput)
                        .build();
                responsePrint(resp, gson.toJson(response)); // Uses inherited method from BaseServlet
                logger.info("Response sent to client");

            } catch (Exception e) {
                logger.error("Image analysis failed", e);
                Response response = Response.builder()
                        .status("failed")
                        .msg("Image analysis failed: " + e.getMessage())
                        .build();
                responsePrint(resp, gson.toJson(response)); // Uses inherited method from BaseServlet
            }
        } else {
            logger.warn("No files uploaded, error message: {}", msg);
            Response response = Response.builder()
                    .status("failed")
                    .msg(msg != null ? msg : "No file uploaded")
                    .build();
            responsePrint(resp, gson.toJson(response)); // Uses inherited method from BaseServlet
        }
    }

    private String analyzeImage(String imageFilePath) throws IOException {
        List<BlockDesc> blockDescList = imageBlockService.analyzeBdImage(imageFilePath);
        StringBuilder sb = new StringBuilder();
        for (BlockDesc blockDesc : blockDescList) {
            sb.append(blockDesc.getId()).append(". ").append(blockDesc.getBlock()).append("\n");
            sb.append(blockDesc.getDescription()).append("\n");
        }
        return sb.toString();
    }

    private String searchVectorDb(String imageAnalysisResult) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        List<BlockItem> blockItems = objectMapper.readValue(imageAnalysisResult, new TypeReference<List<BlockItem>>() {
        });
        sortByBlueMarkedNumber(blockItems);
        StringBuilder sb = new StringBuilder();
        for (BlockItem item : blockItems) {
            String blockDesc = search(item);
            sb.append(blockDesc);
        }
        if (sb.length() == 0) {
            return null;
        }
        return sb.toString();
    }

    private String search(BlockItem item) {
        String query = item.getLargestFontText();
        if (query.trim().isEmpty()) {
            return "";
        }
        String num = item.getBlueMarkedNumber();
        String category = "lumissil-block";
        Map<String, String> metadatas = new HashMap<>();
        metadatas.put("category", category);
        List<List<IndexSearchData>> searchResults1 = vectorStoreService.search(query.toLowerCase(), 1, 0.5, metadatas, category, 1, 1);
        List<List<IndexSearchData>> searchResults2 = vectorStoreService.search(query.toUpperCase(), 1, 0.5, metadatas, category, 1, 1);
        double similarity1 = getMinSimilarity(searchResults1);
        double similarity2 = getMinSimilarity(searchResults2);
        List<List<IndexSearchData>> searchResults;
        if (similarity1 < similarity2) {
            searchResults = searchResults1;
        } else {
            searchResults = searchResults2;
        }
        StringBuilder sb = new StringBuilder(num + ". ");
        if (!searchResults.isEmpty()) {
            List<IndexSearchData> firstResult = searchResults.get(0);
            for (IndexSearchData data : firstResult) {
                sb.append(data.getText());
                sb.append("\n");
            }
            return sb.toString();
        }
        return "";
    }

    private double getMinSimilarity(List<List<IndexSearchData>> searchResults) {
        double minSimilarity = 1.0;
        if (searchResults == null || searchResults.isEmpty()) {
            return minSimilarity;
        }
        for (List<IndexSearchData> result : searchResults) {
            if (result != null && !result.isEmpty()) {
                IndexSearchData firstResult = result.get(0);
                if (firstResult.getDistance() != null && firstResult.getDistance() < minSimilarity) {
                    minSimilarity = firstResult.getDistance();
                }
            }
        }
        return minSimilarity;
    }

    public static void sortByBlueMarkedNumber(List<BlockItem> blockItems) {
        blockItems.sort((item1, item2) -> {
            Integer num1 = parseToInteger(item1.getBlueMarkedNumber());
            Integer num2 = parseToInteger(item2.getBlueMarkedNumber());

            if (num1 == null && num2 == null) return 0;
            if (num1 == null) return 1;
            if (num2 == null) return -1;

            return num1.compareTo(num2);
        });
    }

    private static Integer parseToInteger(String str) {
        if (str == null || str.trim().isEmpty()) {
            return null;
        }

        try {
            return Integer.parseInt(str.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String uploadImageToProcessEndpoint(File imageFile) throws IOException {
        // (Unchanged, use the original implementation)
        String boundary = "===" + System.currentTimeMillis() + "===";
        String LINE_FEED = "\r\n";
        URL url = new URL("http://localhost:8123/process-image");
        HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
        httpConn.setUseCaches(false);
        httpConn.setDoOutput(true);
        httpConn.setDoInput(true);
        httpConn.setRequestMethod("POST");
        httpConn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

        try (OutputStream outputStream = httpConn.getOutputStream();
             PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream, "UTF-8"), true)) {

            String fileName = imageFile.getName();
            writer.append("--" + boundary).append(LINE_FEED);
            writer.append("Content-Disposition: form-data; name=\"file\"; filename=\"" + fileName + "\"").append(LINE_FEED);
            writer.append("Content-Type: " + Files.probeContentType(imageFile.toPath())).append(LINE_FEED);
            writer.append(LINE_FEED).flush();

            Files.copy(imageFile.toPath(), outputStream);
            outputStream.flush();
            writer.append(LINE_FEED).flush();

            writer.append("--" + boundary + "--").append(LINE_FEED);
            writer.flush();

            int responseCode = httpConn.getResponseCode();
            logger.info("Image process endpoint response code: {}", responseCode);
            if (responseCode != 200) {
                throw new IOException("Image processing endpoint returned non-200 status: " + responseCode);
            }

            try (BufferedReader in = new BufferedReader(new InputStreamReader(httpConn.getInputStream()))) {
                StringBuilder response = new StringBuilder();
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                return response.toString();
            }
        } finally {
            httpConn.disconnect();
        }
    }

    private String generateFormattedOutput(String imageAnalysisResult) {
        // (Unchanged, use the Java 8-compatible implementation from previous response)
        logger.info("Generating formatted output with image analysis result");

// Prepare the prompt
        String prompt = String.format(
                "你是一个电子工程文档专家。你的任务是根据提供的图像分析结果和背景信息，生成一个格式化的编号列表。输出必须为一个编号列表，每项包含图像分析结果中的 'largest_font_text'（如果非空）及其在背景信息中的对应描述。如果某个 'largest_font_text' 在背景信息中没有匹配的描述，则完全跳过该项，不在输出中出现。编号必须严格等于 'blue_marked_number' 的值，并按 'blue_marked_number' 的数值从小到大排序。如果 'blue_marked_number' 为空或无效，分配一个连续的编号并放在末尾。输出的术语和描述必须完全遵循背景信息中的原文（包括大小写和格式），不得修改或规范化。\n\n" +
                        "**图像分析结果：**\n%s\n\n" +
                        "**背景信息：**\n%s\n\n" +
                        "**请求的输出格式示例：**\n" +
                        "1. Bias\n" +
                        "To provide the basic voltage reference and current bias for analog block\n" +
                        "2. Regulator\n" +
                        "To provide kinds of power for analog and digital block\n" +
                        "6. current setting\n" +
                        "The current configured parameters or operating values in the device or system\n" +
                        "10. ADC\n" +
                        "Convert some analog input to digital data\n" +
                        "11. Clock\n" +
                        "Output 3 types of clock signal: 1. CLK_SYS=32MHz; 2.CLK_PWM=32MHz spread spectrum optional; 3. 8MHz\n\n" +
                        "**指令：**\n" +
                        "- 处理所有 'largest_font_text' 不为空的项。\n" +
                        "- 编号必须严格等于 'blue_marked_number' 的值（转换为整数，例如，'10' 输出为 '10.'）。\n" +
                        "- 特别纠错：如果 'largest_font_text' 为 'CLOCK' 且 'blue_marked_number' 为 '31'，将 'blue_marked_number' 纠正为 '11'。\n" +
                        "- 特别纠错：如果 'largest_font_text' 为 'Register'，视为 'Digital' 的误识别，使用背景信息中的 'Digital' 描述，并保留其 'blue_marked_number'（例如，'5'）。\n" +
                        "- 按 'blue_marked_number' 的数值从小到大排序（例如，1, 2, 3, ..., 11）。如果 'blue_marked_number' 重复，按照图像分析结果中的出现顺序排列。\n" +
                        "- 如果 'blue_marked_number' 为空或无效，将该项放在末尾，并分配一个连续的编号（从最后一个有效编号开始，例如，若最大有效编号为 11，则分配 12）。\n" +
                        "- 在匹配 'largest_font_text' 与背景信息时，忽略大小写（例如，'BIAS' 匹配 'Bias'，'CLOCK' 匹配 'Clock'），但输出时必须使用背景信息中的术语和大小写（例如，'Bias' 而非 'BIAS'）。\n" +
                        "- 如果 'largest_font_text' 为 'setting'，必须匹配背景信息中的 'current setting'，输出术语为 'current setting'，描述为 'The current configured parameters or operating values in the device or system.'。\n" +
                        "- 如果 'largest_font_text' 在背景信息中没有匹配的描述（在应用纠错后仍无匹配），完全跳过该项，不输出任何内容。\n" +
                        "- 每项输出占两行：第一行是 '编号. 术语'，第二行是背景信息中的完整描述。\n" +
                        "- 确保每项只有一次，避免重复。\n" +
                        "- 确保输出完整，包含所有符合条件的项，编号精确，术语和描述正确，无缺失。\n" +
                        "**示例：**\n" +
                        "如果图像分析结果为：[{\"blue_marked_number\":\"1\",\"largest_font_text\":\"BIAS\"},{\"blue_marked_number\":\"6\",\"largest_font_text\":\"setting\"},{\"blue_marked_number\":\"10\",\"largest_font_text\":\"ADC\"},{\"blue_marked_number\":\"31\",\"largest_font_text\":\"CLOCK\"},{\"blue_marked_number\":\"5\",\"largest_font_text\":\"Register\"}]，则输出为：\n" +
                        "1. Bias\n" +
                        "To provide the basic voltage reference and current bias for analog block\n" +
                        "5. Digital\n" +
                        "To manage digital signal processing and control logic\n" +
                        "6. current setting\n" +
                        "The current configured parameters or operating values in the device or system\n" +
                        "10. ADC\n" +
                        "Convert some analog input to digital data\n" +
                        "11. Clock\n" +
                        "Output 3 types of clock signal: 1. CLK_SYS=32MHz; 2.CLK_PWM=32MHz spread spectrum optional; 3. 8MHz\n" +
                        "（'CLOCK' 的 'blue_marked_number' 从 '31' 纠正为 '11'，'Register' 纠正为 'Digital'）\n",
                imageAnalysisResult, BACKGROUND_INFO
        );
        logger.debug("LLM prompt: {}", prompt);

        ChatCompletionRequest request = new ChatCompletionRequest();
        request.setTemperature(0.05);
        request.setMax_tokens(16384);
        request.setStream(false);

        List<ChatMessage> messages = new ArrayList<>();
        ChatMessage userMessage = new ChatMessage();
        userMessage.setRole("user");
        userMessage.setContent(prompt);
        messages.add(userMessage);

        request.setMessages(messages);

        logger.debug("Sending LLM request with prompt length: {}", prompt.length());
        ChatCompletionResult result = completionsService.completions(request);

        if (result == null || result.getChoices() == null || result.getChoices().isEmpty()) {
            logger.error("LLM call failed: No choices returned");
            return null;
        }

        String llmResponse = result.getChoices().get(0).getMessage().getContent();
        logger.info("LLM response received, length: {}", llmResponse.length());
        return llmResponse;
    }

}
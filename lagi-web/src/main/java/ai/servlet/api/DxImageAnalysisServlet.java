package ai.servlet.api;

import ai.common.pojo.Response;
import ai.llm.service.CompletionsService;
import ai.openai.pojo.ChatCompletionRequest;
import ai.openai.pojo.ChatCompletionResult;
import ai.openai.pojo.ChatMessage;
import ai.servlet.BaseServlet;
import ai.utils.MigrateGlobal;
import ai.vector.FileService;
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
import java.util.List;
import java.util.UUID;

public class DxImageAnalysisServlet extends BaseServlet {
    private static final String UPLOAD_DIR = "/upload";
    private static final String VLIMG_SUBDIR = "/vlimg";
    private static final String BASE_URL = "https://lumissil.saasai.top";
    private static final Logger logger = LoggerFactory.getLogger(DxImageAnalysisServlet.class);
    private final FileService fileService = new FileService();
    private final CompletionsService completionsService = new CompletionsService();
    private final Gson gson = new Gson();

    private static final String BACKGROUND_INFO = "DX001: FaultB\n" +
            "Function: Provides fault feedback to host via FaultB mechanism.Short Description: FaultB pin signals system faults; host must read diagnostic registers periodically.Detailed Description: Every ~40ms (recommended <50ms), the host should issue a read command to the 3138A to maintain safety. This read can determine: 1. If data is received from 3138A (communication check). 2. If fault flags are set via fault type registers (50h/51h), host acts according to specific fault types. 3. If internal temperature (via ADC) exceeds thresholds, host responds accordingly: <-40°C to +65°C: Normal; +65°C to +125°C: Send warning to higher-level system; +125°C to 180°C: Turn on all LEDs to decrease internal temperature. Fault types include: CMWF1 (Communication Watchdog1 Timeout), CMWF2 (Communication Watchdog2 Timeout), TF (Thermal Roll-off), CRCF (UART CRC Fault), RSET_OP (RSET Open), RSET_SH (RSET Short), TSD (Thermal Shutdown), LPBF (PWM Loop Duty Fault), SHORTF (LED Short), OPENF (LED Open), SLSHORTF (Single LED Short), EXF (External Fault), LOF (LDO OV Fault), SCAVF (SCA Data Fault).Detection and Reaction Time: <50msReaction on Fault: If no data is received: host checks communication. If fault is detected: host responds according to the specific fault type. If temperature exceeds thresholds: host takes corresponding thermal mitigation actions.Covers Transient Faults: YesDiagnostic Coverage: 90.00%Comments: See the fault type Register 50h/51h for detailed fault information.\n" +
            "DI001: Open/Short Detection\n" +
            "Function: Detects LED open/short faults and internal power MOS open/short.Short Description: Monitors voltage between LED (+) and LED (-) (VLED) to detect LED open or short faults.Detailed Description: VLED is compared against thresholds. If VLED < (VCC - Vth_short), an LED short fault is triggered. If VOUT < 0.1V, an LED open fault is triggered. Also detects internal power MOS open/short faults.Detection and Reaction Time: <50uS Reaction on Fault: LED short (VLED < (VCC - Vth_short)): Triggers and reports fault.LED open (VOUT < 0.1V): Triggers and reports fault.\n" +
            "Covers Transient Faults: YesDiagnostic Coverage: 90.00%Comments: None\n" +
            "DI002: OV3/UV3 Monitor\n" +
            "Function: Monitors VDD voltage for overvoltage (OV) or undervoltage (UV).Short Description: Detects if VDD voltage is below 3.6V (UV) or above 5.5V (OV).Detailed Description: VDD voltage is checked against thresholds. VDD < 3.6V triggers VDD UV fault; VDD > 5.5V triggers VDD OV fault.Detection and Reaction Time: <50uS Reaction on Fault: VDD UV: faultB pin pulls low, related register bit sets high, all registers reset.VDD OV: faultB pin pulls low, related register bit sets high.\n" +
            "Covers Transient Faults: YesDiagnostic Coverage: 90.00%Comments: None\n" +
            "DI003: PWM Loop Duty Test\n" +
            "Function: Compares commanded and actual PWM dimming for LED.Short Description: Checks if PWM dimming difference exceeds tolerance, reporting a fault if out of range.Detailed Description: Compares commanded PWM dimming with actual LED PWM dimming. If difference exceeds tolerance, a fault is reported.Detection and Reaction Time: <10msReaction on Fault: Sets related register bit high or pulls faultB pin low. Host determines whether to continue LED dimming.Covers Transient Faults: YesDiagnostic Coverage: 90.00%Comments: None\n" +
            "DI004: PWM Loop Duty Injection Test\n" +
            "Function: Tests DI003 functionality by injecting PWM signals.Short Description: Host injects PWM=0 or PWM=1 to verify DI003 operation.Detailed Description: Host injects PWM=0 or PWM=1 to confirm DI003 PWM loop duty test functionality.Detection and Reaction Time: <1msReaction on Fault: Reports fault; host reads fault status.Covers Transient Faults: NoDiagnostic Coverage: 60.00%Comments: None\n" +
            "DI005: Logic Built-In Self-Test (LBIST)\n" +
            "Function: Performs digital logic self-test on power-up.Short Description: Executes LBIST after power supply is ready during power-up.Detailed Description: On power-up, digital logic runs LBIST once power supply is stable.Detection and Reaction Time: <500uS Reaction on Fault: Host reads register to check LBIST pass/fail. If pass, proceeds with programmed dimming; if fail, may require another power-up or report to higher-level system.Covers Transient Faults: NoDiagnostic Coverage: 60.00%Comments: None\n" +
            "DI006: Watchdog\n" +
            "Function: Monitors communication timeouts using Watchdog1 and Watchdog2.Short Description: Watchdog1 timeouts on no communication; Watchdog2 timeouts if communication misses a configurable timing window.Detailed Description: Watchdog1 triggers on prolonged lack of communication. Watchdog2 triggers if communication falls outside a register-set timing window. No communication means no correct CRC completion.Detection and Reaction Time: <40msReaction on Fault: Watchdog1 or Watchdog2 timeout triggers IC to enter safe mode.Covers Transient Faults: YesDiagnostic Coverage: 90.00%Comments: None\n" +
            "DI007: CRC (Cyclic Redundancy Check)\n" +
            "Function: Validates data frame integrity using CRC.Short Description: Checks CRC in data frame for validity in both received and transmitted data.Detailed Description: CRC is included at the end of data frames. If CRC is incorrect, the frame is invalid. Applies to both received and transmitted data.Detection and Reaction Time: <500uS Reaction on Fault: faultB pin pulls low.Related register bit sets high.CRC accumulation counter increments by 1.\n" +
            "Covers Transient Faults: YesDiagnostic Coverage: 90.00%Comments: None\n" +
            "DI008: VIN Under Voltage Detection\n" +
            "Function: Monitors VIN voltage for undervoltage (UV).Short Description: Detects if VIN voltage is below 3.5V (VIN UV).Detailed Description: VIN voltage is checked; if VIN < 3.5V, a VIN UV fault is triggered.Detection and Reaction Time: <50uS Reaction on Fault: Resets all outputs, faultB status, and registers to default values.Covers Transient Faults: YesDiagnostic Coverage: 90.00%Comments: None\n" +
            "DI009: CLK Detect\n" +
            "Function: Detects CLK signal errors.Short Description: Identifies CLK as constant high or low for >16uS , indicating an error.Detailed Description: If CLK is constant high or low for >16uS  (counted by 8MHz clock), 3138A considers it a CLK output error.Detection and Reaction Time: <100uS Reaction on Fault: Reports fault and replaces CLK_PWM with CLK_SYS.Covers Transient Faults: YesDiagnostic Coverage: 90.00%Comments: None\n" +
            "DI010: Thermal Shutdown\n" +
            "Function: Monitors internal temperature for thermal shutdown.Short Description: Detects if internal temperature exceeds 170C.Detailed Description: Continuously monitors internal temperature. If it exceeds 170C, triggers thermal shutdown.Detection and Reaction Time: <100uS Reaction on Fault: Turns off output channel and reports fault to host.Covers Transient Faults: YesDiagnostic Coverage: 90.00%Comments: None\n" +
            "DI011: ADC\n" +
            "Function: Converts analog signals to digital data for monitoring.Short Description: Converts 26 key analog signals to digital data in registers.Detailed Description: Converts 26 important analog signals to digital data stored in registers. Host can force a reference voltage to monitor ADC function and compare results.Detection and Reaction Time: <10msReaction on Fault: Host detects errors by comparing ADC values.Covers Transient Faults: YesDiagnostic Coverage: 90.00%Comments: None\n" +
            "DI012: Output Peak Current Monitor\n" +
            "Function: Monitors output channel peak current.Short Description: Senses output channel current, converts to voltage, and samples via ADC.Detailed Description: Output channel current is sensed and converted to a voltage sampled by ADC. Host reads ADC register to obtain output peak current and compares values.Detection and Reaction Time: <10msReaction on Fault: Host detects errors by comparing ADC register values.Covers Transient Faults: YesDiagnostic Coverage: 90.00%Comments: None\n" +
            "DI013: Output Peak Current Monitor BIST\n" +
            "Function: Verifies DI012 functionality via built-in self-test.Short Description: Forces 6uA bias current to test output peak current detection after power-up.Detailed Description: Forces 6uA bias current to verify DI012 output peak current monitor function after power-up.Detection and Reaction Time: <1msReaction on Fault: Host reads ADC register, compares values, and detects errors.Covers Transient Faults: NoDiagnostic Coverage: 60.00%Comments: None\n" +
            "DI014: SCA Data Injection Test\n" +
            "Function: Tests low voltage power MOS ON/OFF status.Short Description: Forces SCA data to verify power MOS control after power-up.Detailed Description: Forces SCA data to test low voltage power MOS ON/OFF status. Continuously monitors all power MOS gates controlled by 7-bit SCA data.Detection and Reaction Time: <10msReaction on Fault: Reports fault at faultB pin; host reads fault status via fault register.Covers Transient Faults: YesDiagnostic Coverage: 90.00%Comments: None\n" +
            "DI015: RSET Open/Short Detect\n" +
            "Function: Detects RSET pin open or short faults.Short Description: Monitors RSET pin voltage or current for open/short status.Detailed Description: Detects RSET pin open fault if voltage > 3V; detects RSET short fault if current > 310uA.Detection and Reaction Time: <100uS Reaction on Fault: RSET open (voltage > 3V): Asserts RSET open fault.RSET short (current > 310uA): Asserts RSET short fault.\n" +
            "Covers Transient Faults: YesDiagnostic Coverage: 90.00%Comments: None\n" +
            "DI016: ECC (Error Correcting Code)\n" +
            "Function: Detects and corrects errors in 512-bit OTP.Short Description: Uses ECC to detect and fix 1-bit errors in 512-bit OTP.Detailed Description: Error Correcting Code detects and corrects 1-bit errors in 512-bit OTP memory.Detection and Reaction Time: <100uS Reaction on Fault: Corrects 1-bit errors in 512-bit OTP.Covers Transient Faults: YesDiagnostic Coverage: 90.00%Comments: None\n" +
            "DI017: V5/V33 Under Voltage Monitor\n" +
            "Function: Monitors V5 and V33 voltages for undervoltage.Short Description: Detects if V5 < 3.1V or V33 < 2.5V (UV).Detailed Description: Checks V5 and V33 voltages. V5 < 3.1V triggers V5 UV; V33 < 2.5V triggers V33 UV.Detection and Reaction Time: <1msReaction on Fault: Resets all registers and disables communication until V5 > 3.3V and V33 > 2.7V. Host gets no response if issuing read commands.Covers Transient Faults: YesDiagnostic Coverage: 99.00%Comments: Analog comparator ensures high DC level; detection is full-time.\n" +
            "DI018: V5/V33 Overvoltage Monitor\n" +
            "Function: Monitors V5 and V33 voltages for overvoltage.Short Description: Detects if V5 or V33 exceeds 6.5V (OV).Detailed Description: Checks if V5 or V33 voltage exceeds 6.5V, triggering V5/V33 OV fault.Detection and Reaction Time: <50uS Reaction on Fault: faultB pin pulls low.Related register bit sets high.\n" +
            "Covers Transient Faults: YesDiagnostic Coverage: 99.00%Comments: Analog comparator ensures high DC level; detection is full-time.\n" +
            "DI019: Charge Pump Under Voltage Detect\n" +
            "Function: Monitors charge pump output voltage for undervoltage.Short Description: Detects if charge pump output for 12 channels is < 3.3V.Detailed Description: Monitors charge pump output voltage for 12 channels. If < 3.3V, reports UV to all channels.Detection and Reaction Time: <50uS Reaction on Fault: Affected channel sets driver output to constant low; no fault report.Covers Transient Faults: YesDiagnostic Coverage: 99.00%Comments: Analog comparator ensures high DC level; detection is full-time.\n" +
            "DI020: PIN Double Bondings\n" +
            "Function: Ensures redundancy via double bondings on VCC and V5.Short Description: VCC has two bondings on one pad; V5 has two bondings on two pads.Detailed Description: VCC uses two bondings on a single pad; V5 uses two bondings on separate pads for redundancy.Detection and Reaction Time: N/AReaction on Fault: N/ACovers Transient Faults: YesDiagnostic Coverage: 99.00%Comments: Double bonding is a redundancy design with high DC level.";

    private static final String PROMPT = "private static final String PROMPT = \"You are provided with the following two inputs:\\n\" +\n" +
            "    \"1. **Image analysis result**: A JSON array from a vision model. Each object contains:\\n\" +\n" +
            "    \"   - `id`: A string identifier (e.g., \\\"DI001\\\", \\\"DX002\\\")\\n\" +\n" +
            "    \"   - `description`: A short textual label or function name (e.g., \\\"ADC\\\", \\\"VIN under voltage detection\\\")\\n\" +\n" +
            "    \"```\\n\" +\n" +
            "    \"%s\\n\" +  \n" +
            "    \"```\\n\" +\n" +
            "    \"\\n\" +\n" +
            "    \"2. **Background information**: A block of text containing multiple entries. Each entry starts with an identifier like `DI001`, `DI002`, etc., and includes fields such as `Function`, `Short Description`, and `Detailed Description`.\\n\" +\n" +
            "    \"```\\n\" +\n" +
            "    \"%s\\n\" +  \n" +
            "    \"```\\n\" +\n" +
            "    \"\\n\" +\n" +
            "    \"Your task:\\n\" +\n" +
            "    \"1. For each `id` present in the JSON array, **look for an exact match** in the background information.\\n\" +\n" +
            "    \"2. If a match is found, extract only the value of the `Detailed Description` field from that entry.\\n\" +\n" +
            "    \"3. If no exact match is found, return the message: \\\"No matching description found\\\".\\n\" +\n" +
            "    \"\\n\" +\n" +
            "    \"**Important Constraints**:\\n\" +\n" +
            "    \"- Do not convert or infer IDs (e.g., do not change DXxxx → DIxxx).\\n\" +\n" +
            "    \"- Only process the identifiers present in the vision model's JSON array.\\n\" +\n" +
            "    \"- Do not return or summarize unrelated entries from the background information.\\n\" +\n" +
            "    \"- Do not return JSON. Output must be plain text.\\n\" +\n" +
            "    \"\\n\" +\n" +
            "    \"Output format:\\n\" +\n" +
            "    \"```\\n\" +\n" +
            "    \"<id>: <Detailed Description>\\n\" +\n" +
            "    \"```\\n\" +\n" +
            "    \"Example:\\n\" +\n" +
            "    \"```\\n\" +\n" +
            "    \"DI001: Monitors VIN voltage level. If VIN drops below threshold, triggers undervoltage fault.\\n\" +\n" +
            "    \"DX003: No matching description found\\n\" +\n" +
            "    \"DI005: Provides internal communication watchdog and timeout logic.\\n\" +\n" +
            "    \"```\";\n";


    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        logger.info("Received POST request: {}", req.getRequestURI());
        req.setCharacterEncoding("UTF-8");
        resp.setHeader("Content-Type", "application/json;charset=utf-8");
        String url = req.getRequestURI();
        String method = url.substring(url.lastIndexOf("/") + 1);
        logger.debug("Extracted method: {}", method);

        if (method.equals("dxAnalyzeImage")) {
            this.analyzeImage(req, resp);
        } else {
            logger.warn("Unsupported method: {}", method);
            Response response = Response.builder().status("failed").msg("Unsupported method").build();
            responsePrint(resp, gson.toJson(response));
        }
    }

    private void analyzeImage(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        logger.info("Starting DX image analysis");
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
            File imageFile = files.get(0);
            logger.info("Processing image file: {}", imageFile.getName());
            File destFile = null;
            try {
                // Step 1: Upload image and get URL
                String imageUrl = uploadFileAndGetUrl(imageFile);
                if (imageUrl == null || imageUrl.trim().isEmpty()) {
                    throw new RuntimeException("Failed to upload image and get URL");
                }
                logger.info("Image URL generated: {}", imageUrl);

                // Step 2: Call visual model to analyze the image
                String imageAnalysisResult = analyzeImageWithVisionModel(imageUrl);
                if (imageAnalysisResult == null || imageAnalysisResult.trim().isEmpty()) {
                    throw new RuntimeException("Failed to analyze image with vision model");
                }
                logger.info("Image analysis result received, length: {}", imageAnalysisResult.length());

                // Step 3: Generate formatted output using language model
                String formattedOutput = generateFormattedOutput(imageAnalysisResult);
                if (formattedOutput == null || formattedOutput.trim().isEmpty()) {
                    throw new RuntimeException("Failed to generate formatted output");
                }
                logger.info("Formatted output generated, length: {}", formattedOutput.length());

                // Step 4: Return the result
                Response response = Response.builder()
                        .status("success")
                        .data(formattedOutput)
                        .build();
                responsePrint(resp, gson.toJson(response));
                logger.info("Response sent to client");

            } catch (Exception e) {
                logger.error("DX image analysis failed", e);
                Response response = Response.builder()
                        .status("failed")
                        .msg("Image analysis failed: " + e.getMessage())
                        .build();
                responsePrint(resp, gson.toJson(response));
            } finally {
                // Cleanup uploaded file
                if (imageFile != null && imageFile.exists()) {
                    try {
                        Files.delete(imageFile.toPath());
                        logger.debug("Cleaned up uploaded image: {}", imageFile.getName());
                    } catch (IOException e) {
                        logger.warn("Failed to delete uploaded image: {}", imageFile.getName(), e);
                    }
                }
                // Cleanup destination file (created by uploadFileAndGetUrl)
                if (destFile != null && destFile.exists()) {
                    try {
                        Files.delete(destFile.toPath());
                        logger.debug("Cleaned up destination image: {}", destFile.getName());
                    } catch (IOException e) {
                        logger.warn("Failed to delete destination image: {}", destFile.getName(), e);
                    }
                }
            }
        } else {
            logger.warn("No files uploaded, error message: {}", msg);
            Response response = Response.builder()
                    .status("failed")
                    .msg(msg != null ? msg : "No file uploaded")
                    .build();
            responsePrint(resp, gson.toJson(response));
        }
    }

    private String uploadFileAndGetUrl(File file) throws IOException {
        logger.info("Uploading file and generating URL: {}", file.getName());

        // 获取 Web 应用的根目录
        String webappDir = getServletContext().getRealPath("/");
        String imageDir = webappDir + UPLOAD_DIR + VLIMG_SUBDIR;

        // 确保目录存在
        File dir = new File(imageDir);
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                throw new IOException("Failed to create directory: " + imageDir);
            }
            logger.info("Created directory: {}", imageDir);
        }

        // 生成唯一文件名，防止覆盖
        String originalName = file.getName();
        String extension = originalName.substring(originalName.lastIndexOf("."));
        String uniqueName = UUID.randomUUID().toString() + extension;
        File destFile = new File(imageDir, uniqueName);

        // 移动文件到目标目录
        if (!file.renameTo(destFile)) {
            throw new IOException("Failed to move file to: " + destFile.getAbsolutePath());
        }
        logger.debug("File moved to: {}", destFile.getAbsolutePath());

        // 生成公开 URL
        String relativePath = UPLOAD_DIR + VLIMG_SUBDIR + "/" + uniqueName;
        String imageUrl = BASE_URL + relativePath;
        logger.debug("Generated image URL: {}", imageUrl);

        // Store destFile for cleanup
        this.destFile = destFile; // Store in instance variable for cleanup in analyzeImage
        return imageUrl;
    }

    // Instance variable to store destFile for cleanup
    private File destFile;

    private String analyzeImageWithVisionModel(String imageUrl) throws IOException {
        logger.info("Analyzing image with vision model: {}", imageUrl);

        // Prepare JSON request
        String visionPrompt = "Carefully analyze the provided image content and perform the following steps:\n" +
                "\n" +
                "1. **Scan the entire image thoroughly**: Identify and record all item identifiers that start with the letter 'D' followed by any combination of letters and digits (e.g., 'DX001', 'DI002', etc.).\n" +
                "\n" +
                "2. **Include all valid identifiers**: Include all identifiers that begin with 'D' (e.g., 'DI', 'DX') — do not exclude any relevant items just because they are not 'DX'.\n" +
                "\n" +
                "3. **Associate text descriptions**: For each identified identifier, locate and extract the corresponding functional name or textual description that is visually linked with it (e.g., 'ADC', 'VIN under voltage detection', 'PWM loop duty test'). If multiple texts are associated, choose the most directly relevant.\n" +
                "\n" +
                "4. **Ensure accuracy**: Make sure the extracted information exactly matches the visual content of the image — do not guess or infer.\n" +
                "\n" +
                "Return the result as a JSON array, where each object contains the following fields:\n" +
                "- `\"id\"`: A string representing the identifier (e.g., 'DI001' or 'DX001').\n" +
                "- `\"description\"`: A string representing the associated function name or textual description.\n" +
                "\n" +
                "Example output format:\n" +
                "```json\n" +
                "[\n" +
                "  {\n" +
                "    \"id\": \"DX001\",\n" +
                "    \"description\": \"Flag\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"id\": \"DI002\",\n" +
                "    \"description\": \"OV3/UV3\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"id\": \"DI010\",\n" +
                "    \"description\": \"Thermal Shutdown\"\n" +
                "  }\n" +
                "]\n" +
                "```\n" +
                "\n" +
                "Do not return anything other than the JSON array. Focus only on identifiers that start with the letter 'D'.";
        VisionRequest request = new VisionRequest(imageUrl, visionPrompt);
        String requestBody = gson.toJson(request);
        logger.debug("Vision model request body: {}", requestBody);

        // Call visual model service
        HttpURLConnection httpConn = null;
        try {
            URL url = new URL("http://localhost:8125/analyze");
            httpConn = (HttpURLConnection) url.openConnection();
            httpConn.setRequestMethod("POST");
            httpConn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            httpConn.setDoOutput(true);

            // Send request body
            try (OutputStream os = httpConn.getOutputStream()) {
                byte[] input = requestBody.getBytes("UTF-8");
                os.write(input, 0, input.length);
            }

            // Read response
            int responseCode = httpConn.getResponseCode();
            logger.info("Vision model service response code: {}", responseCode);

            if (responseCode != 200) {
                try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(httpConn.getErrorStream()))) {
                    StringBuilder errorResponse = new StringBuilder();
                    String line;
                    while ((line = errorReader.readLine()) != null) {
                        errorResponse.append(line);
                    }
                    logger.error("Vision model error response: {}", errorResponse.toString());
                    throw new IOException("Vision model service returned non-200 status: " + responseCode + ", error: " + errorResponse.toString());
                }
            }

            try (BufferedReader in = new BufferedReader(new InputStreamReader(httpConn.getInputStream()))) {
                StringBuilder response = new StringBuilder();
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                String jsonResponse = response.toString();
                logger.info("Vision model response received, length: {}", jsonResponse.length());

                // Parse JSON response to extract 'result' field
                VisionResponse visionResponse = gson.fromJson(jsonResponse, VisionResponse.class);
                if (visionResponse.getResult() == null) {
                    throw new IOException("Vision model response missing 'result' field");
                }
                return visionResponse.getResult();
            }
        } catch (IOException e) {
            logger.error("Failed to call vision model service", e);
            throw e;
        } finally {
            if (httpConn != null) {
                httpConn.disconnect();
                logger.debug("Vision model connection closed");
            }
        }
    }

    private String generateFormattedOutput(String imageAnalysisResult) {
        logger.info("Generating formatted output with image analysis result");

        // Prepare the prompt
        String prompt = String.format(PROMPT, imageAnalysisResult, BACKGROUND_INFO);
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

    // Request class for vision model
    private static class VisionRequest {
        private String image_url;
        private String prompt;

        public VisionRequest(String image_url, String prompt) {
            this.image_url = image_url;
            this.prompt = prompt;
        }

        public String getImage_url() { return image_url; }
        public void setImage_url(String image_url) { this.image_url = image_url; }
        public String getPrompt() { return prompt; }
        public void setPrompt(String prompt) { this.prompt = prompt; }
    }

    // Response class for vision model
    private static class VisionResponse {
        private String result;

        public String getResult() { return result; }
        public void setResult(String result) { this.result = result; }
    }
}
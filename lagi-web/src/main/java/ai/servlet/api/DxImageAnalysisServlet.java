package ai.servlet.api;

import ai.common.pojo.IndexSearchData;
import ai.common.pojo.Response;
import ai.dto.RuleTxt;
import ai.llm.service.CompletionsService;
import ai.openai.pojo.ChatCompletionRequest;
import ai.openai.pojo.ChatCompletionResult;
import ai.openai.pojo.ChatMessage;
import ai.servlet.BaseServlet;
import ai.sevice.DxImageService;
import ai.utils.JsonExtractor;
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
import java.util.*;
import java.util.stream.Collectors;

public class DxImageAnalysisServlet extends BaseServlet {
    private static final String UPLOAD_DIR = "/upload";
    private static final String VLIMG_SUBDIR = "/vlimg";
    private static final String BASE_URL = "https://lumissil.saasai.top";
    private static final Logger logger = LoggerFactory.getLogger(DxImageAnalysisServlet.class);
    private final FileService fileService = new FileService();
    private final CompletionsService completionsService = new CompletionsService();
    private final Gson gson = new Gson();
    private final VectorStoreService vectorStoreService = new VectorStoreService();
    private final DxImageService dxImageService = new DxImageService();

    private static final String BACKGROUND_INFO = "ID Diagnosis\tShort description of diagnostic procedure\tDetailed description of diagnostic procedure\tDetection and reaction time\tReaction in case diagnostics detects a fail\t\"Covers transient faults\n" +
            "(Yes / No / Partly)\n" +
            "[Drop-down]\"\t\"Diagnostic Coverage\n" +
            "estimated\"\tComments\n" +
            "DI001\tOpen/Short detection\tthe voltage between LED (+) and LED (-), called as VLED is detected. When VLED is lower than (VCC-Vth_short), then LED short fault will be triggered. When VOUT is lower than 0.1V, then LED open fault will be triggered.  This function also can be detected internal power MOS short or open\t<50uS\twhen VLED< (VCC-Vth_short),  LED short will trigger and report fault.                                                           When VOUT<0.1V LED open will trigger and report fault.\tYes\t90.00%\t\n" +
            "DI002\tOV3/UV3 monitor\t\"VDD voltage will be detected to check whether it is lower or higher than a level                                                             VDD<3.6V means VDD UV\n" +
            "VDD>5.5V means VDD OV\n" +
            "\"\t<50uS\tif VDD UV occur, then                                                    1. faultB will be pull low                                                        2. Related Register bit will set high                             3. all register will be reset.                                                         f VDD OV occur, then items 1,2 will action.                         \tYes\t90.00%\t\n" +
            "DI003\tPWM loop duty test\tcompare the PWM dimming of command and actual PWM dimming of LED, if the difference is out of tolerance, report fault. \t<10mS\twhen report fault, either the related Register bit will be set high or the FaultB pin will be pull low.                                                        The host shuold determine whether or not to go on dimming LED.\tYes\t90.00%\t\n" +
            "DI004\tPWM loop duty injection test\thost can inject a PWM=0 or PWM=1 to confirm whether DI003 works\t<1mS\twill report a fault and host can read the fault status \tNo\t60.00%\t\n" +
            "DI005\tLogic build-in self-test\tevery time power up, digital will do Lbist after power supply is ready.\t<500uS\t\"Host can read related Register to check whether Lbist pass or fail.\n" +
            "If pass, go on to do prorammed dimming.\n" +
            " If fail, maybe need another power up or report to the high level system.\"\tNo\t60.00%\t\n" +
            "DI006\tWatchdog\t\"If there is no communication for a long time, Watchdog1 will be timeout.\n" +
            "If there is no communication for a timing window whose maximum and minimum value can be  set by register, Watchdog2 will be timeout.\n" +
            "\"\t<40mS\tWatchdog1 and Watchdog2 timeout will trigger and make IC enter safe mode. No communica-tion means that no correct CRC complete.\tYes\t90.00%\t\n" +
            "DI007\tCRC: Cyclic Redundancy Check\tThe data format has CRC in the frame end, if CRC is not correct, the total data frame is consid-ered as invalid. Note that CRC check not only be in receive data, but also in transmit data.\t<500us\tif one frame has CRC fault, there are 3 actions                                                                                   1. faultB will be pull low.                                                   2. Related Register bit will set high.                                     3. CRC accumulation counter will +1.                               \tYes\t90.00%\t\n" +
            "DI008\tVIN under voltage detection \tVIN voltage will be detected to check whether it is lower than a level                                                             VIN<3.5V means VIN UV\t<50uS\treset all outputs and faultb status, reset all registers to default value.\tYes\t90.00%\t\n" +
            "DI009\tCLK Detect\twhen CLK is detected as constant high or low for more than 16us which is counted by 8MHz clock, 3138A will consider CLK output as an error.\t<100uS\twill report a fault and replace CLK_PWM as CLK_SYS\tYes\t90.00%\t\n" +
            "DI010\tThermal shutdown\tThe internal temperature is always detected to check whether it pass 170C\t<100uS\tIf it exceeds 170C, the output channel will turn-off and report related fault to host\tYes\t90.00%\t\n" +
            "DI011\tADC\tConvert 26 important analog signals to digital data in Register \t<10mS\thost can force a reference voltage to monitor ADC function, then compare it. Host can pick up error\tYes\t90.00%\t\n" +
            "DI012\tOutput peak current monitor\t\"Sense output channel current and convert into a voltage which can be sampled by ADC.\n" +
            "Host can read the ADC register to get the output peak current\n" +
            "\"\t<10mS\thost can read the output peak current by ADC register, then compare the value, host can pick up the error\tYes\t90.00%\t\n" +
            "DI013\tOutput peak current monitor BIST\t\"Force 6uA current as a bias current of output peak current detect block, then it can verify \n" +
            "DI012 function after power up. \n" +
            "\"\t<1mS\thost can read the output peak current by ADC register, then compare the value, host can pick up the error\tNo\t60.00%\t\n" +
            "DI014\tSCA DATA injection test\t\"Force SCA data to test low voltage power MOS ON/OFF status after power up.\n" +
            "Will always monitor all power MOS gate whether it is controlled by 7 bits SCA data\n" +
            "\"\t<10mS\twill report a fault at faultb pin and host can read back the fault stauts by fault register.\tYes\t90.00%\t\n" +
            "DI015\tRSET OPEN/SHORT Detect\twill detect RSET PIN OPEN or short status by RSET pin voltage or current.\t<100uS\t\"When RSET PIN voltage is higher than 3V, then RSET OPEN fault will be asserted.\n" +
            "When RSET PIN current is larger than 310uA, then RSET SHORT fault will be asserted\n" +
            "\"\tYes\t90.00%\t\n" +
            "DI016\tECC\tError Correcting Code that can detect and fix its own errors \t<100uS\tWhen 512-bit OTP has 1-bit error, ECC can correct it.\tYes\t90.00%\t\n" +
            "DI017\tV5/V33 under voltage monitor\tV5V33 voltage will be detect to check whether it is lower than a level                                                             V5<3.1V means V5 UV                                                             V33<2.5V means V33 UV\t<1ms\tIf V5<3.1V or V33<2.5V, all the Register will be reset and can't communication until V5>3.3V&V5>2.7V                                                 eg. disable communcation channel, so Host will get no response if issue read command. Host shall issue read command periodically, frequency < FTTI)\tYes\t99.00%\tanalog comparator is always considered as high DC level and the detect is full time.\n" +
            "DI018\tV5/V33 overvoltage monitor\tdetect whether V5/V33 exceed 6.5V\t<50us\tif V5/V33 OV accur, then                                                    1. faultB will be pull low                                                        2. Related Register bit will set high\tYes\t99.00%\tanalog comparator is always considered as high DC level and the detect is full time.\n" +
            "DI019\tcharge pump under voltage detect\tthe charge pump output for 12 different channels power supply is always detected, if less than 3.3V, will report to every channels.\t<50us\twhen any of the total 12 channels receives charge pump UV signal ,this channel will make driver output constant low.                                                No fault report\tYes\t99.00%\tanalog comparator is always considered as high DC level and the detect is full time.\n" +
            "DI020\tPIN double bondings\tVCC: two bondings on one PAD                                       V5: two bonding on two different PADs\tNA\tNA\tYes\t99.00%\tdouble bonding is a redundancy design which the DC is always considered as high. \n" +
            "DX001\tFaultB\t\"every some time, maybe 40ms, host should send a read command to 3138A to get feedback to achieve safety machanism. The feedback information can be classified as 3 types:\n" +
            "1. whether there is read data from 3138A to host. \n" +
            "2. read Fault type Register to check whether any Fault happens.\n" +
            "3. read ADC related data to check 3138A internal temperature.\"\tcustomer depend, should be<50ms (describe the time for the read of recommended registers)\t\"Different types will have different action. For example,\n" +
            "If 1 happens, there should be error in communication. Host should check communication.\n" +
            "If 2 happens, host will act according to different fault. see note1 for fault type detail information. (FaultB flag)\n" +
            "If 3 happens, host will act according to different temp, such as \n" +
            " <-40C~+65C>, normal. \n" +
            " <+65C~+125C>, send warning to the higher level system.\n" +
            " <+125C~180C>, turn on all the LED to derease the internal temp.                                                                       \"\tYes\t90.00%\t\"See the fault type Register 50h/51h at the bottom of the sheet                                                                                   CMWF1:  communication watch dog1 timeout fault                                      CMWF2:  communication watch dog2 timeout fault                   TF: thermal roll off fault                                                             CRCF: UART communication CRC fault                    RSET_OP: RSET PIN OPEN fault                                       RSET_SH: RSET PIN Short fault                                           TSD: thermal shutdown fault                                                                                      LPBF: PWM loop duty fault                                                           SHORTF: LED short fault                                                         OPENF:  LED open fault                                              SLSHORTF: single LED short  fault                                       EXF: external fault                                                                LOF: LDO OV fault                                                                         SCAVF: SCA data fault\n" +
            "\n" +
            "\"\n";

    private static final String PROMPT = "You are provided with the following two inputs:\n" +
            "1. **Image analysis result**: A JSON array from a vision model. Each object contains:\n" +
            "   - `id`: A string identifier (e.g., \"DI001\", \"DX001\", \"DI016\")\n" +
            "   - `description`: A short textual label or function name (e.g., \"Open/Short Detect\", \"FaultB Flag\", \"ADC\")\n" +
            "```\\n" +
            "%s\\n" +
            "```\\n" +
            "\n" +
            "2. **Background information**: A block of text containing multiple entries in a tabular format. Each entry starts with an identifier like `DI001`, `DI002`, etc., and includes fields such as `Short description of diagnostic procedure` and `Detailed description of diagnostic procedure`. The text is structured with each entry clearly separated and labeled.\n" +
            "```\\n" +
            "%s\\n" +
            "```\\n" +
            "\n" +
            "Your task:\n" +
            "1. For each `id` present in the JSON array, search for an exact match in the background information based solely on the identifier.\n" +
            "2. For every matched `id`, extract the complete and unedited `Short description of diagnostic procedure` and `Detailed description of diagnostic procedure` fields from the corresponding entry in the background information. Every `id` from the JSON array must have a corresponding `Short description of diagnostic procedure` and `Detailed description of diagnostic procedure` from the background information.\n" +
            "\n" +
            "**Important Constraints**:\n" +
            "- Do not modify, infer, or convert IDs (e.g., do not treat 'DX001' as 'DI001' or vice versa).\n" +
            "- Process only the identifiers provided in the vision model's JSON array, ignoring all other background information.\n" +
            "- Ensure every `id` from the JSON array is matched with its `Short description of diagnostic procedure` and `Detailed description of diagnostic procedure`, with no omissions or exceptions.\n" +
            "- Output must be plain text, with no JSON formatting.\n" +
            "- Do not paraphrase or modify the extracted descriptions; they must be verbatim from the background information.\n" +
            "\n" +
            "Output format:\n" +
            "```\\n" +
            "<id>: Short Description: <Short description of diagnostic procedure>\n" +
            "      Detailed Description: <Detailed description of diagnostic procedure>\n" +
            "```\\n" +
            "Example outputs:\n" +
            "```\\n" +
            "DI001: Short Description: Open/Short detection\n" +
            "       Detailed Description: the voltage between LED (+) and LED (-), called as VLED is detected. When VLED is lower than (VCC-Vth_short), then LED short fault will be triggered. When VOUT is lower than 0.1V, then LED open fault will be triggered. This function also can be detected internal power MOS short or open\n" +
            "DX001: Short Description: FaultB\n" +
            "       Detailed Description: every some time, maybe 40ms, host should send a read command to 3138A to get feedback to achieve safety machanism. The feedback information can be classified as 3 types:\n" +
            "1. whether there is read data from 3138A to host.\n" +
            "2. read Fault type Register to check whether any Fault happens.\n" +
            "3. read ADC related data to check 3138A internal temperature.\n" +
            "DI016: Short Description: ECC\n" +
            "       Detailed Description: Error Correcting Code that can detect and fix its own errors\n" +
            "```\n" +
            "```\\n" +
            "DI010: Short Description: Thermal shutdown\n" +
            "       Detailed Description: The internal temperature is always detected to check whether it pass 170C\n" +
            "DI011: Short Description: ADC\n" +
            "       Detailed Description: Convert 26 important analog signals to digital data in Register\n" +
            "```\n" +
            "```\\n" +
            "DI003: Short Description: PWM loop duty test\n" +
            "       Detailed Description: compare the PWM dimming of command and actual PWM dimming of LED, if the difference is out of tolerance, report fault.\n" +
            "DI004: Short Description: PWM loop duty injection test\n" +
            "       Detailed Description: host can inject a PWM=0 or PWM=1 to confirm whether DI003 works\n" +
            "```\n";


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
                String formattedOutput = dxImageService.getAnalyzeImageResult(imageFile.getAbsolutePath());
                if (formattedOutput == null || formattedOutput.trim().isEmpty()) {
                    throw new RuntimeException("Failed to generate formatted output");
                }
                logger.info("Formatted output generated, length: {}", formattedOutput.length());

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
//        imageUrl = "https://lumissil.saasai.top/upload/vlimg/1bff935a-83c6-4313-aad8-b2a71bf36173.png";
//        imageUrl = "https://lumissil.saasai.top/upload/vlimg/b4515d9a-41ec-4284-b095-b1582846c1be.png";
        logger.debug("Generated image URL: {}", imageUrl);

        return imageUrl;
    }


    private String analyzeImageWithVisionModel(String imageUrl) throws IOException {
        logger.info("Analyzing image with vision model: {}", imageUrl);

        // Prepare JSON request
        String visionPrompt = "Carefully analyze the provided image content and perform the following steps:\n" +
                "\n" +
                "1. **Scan the entire image thoroughly**: Identify and record all item identifiers that start with the letter 'D' followed by any combination of letters and digits (e.g., 'DX001', 'DI002', 'DI016'). Scan all regions of the image, including the left, right, top, and bottom areas, to ensure no identifier is missed. Pay special attention to identifiers associated with labels like 'FaultB Flag' or similar annotations, as they may be critical. Ensure every visible identifier is captured without omission, regardless of its prefix ('DI', 'DX', etc.).\n" +
                "\n" +
                "2. **Verify identifiers**: Cross-check each identified identifier against the image to confirm its exact spelling, visibility, and completeness. Ensure identifiers like 'DX001' (often associated with 'FaultB Flag') are explicitly validated. Exclude any identifier that is ambiguous, partially visible, or unreadable. Validate each identifier’s presence with high precision.\n" +
                "\n" +
                "3. **Associate text descriptions**: For each confirmed identifier, locate the single most directly associated functional name or textual description within the image (e.g., 'Open/Short Detect', 'FaultB Flag', 'Thermal Shutdown'). Prioritize text connected via lines or in closest proximity. For example, 'DX001' is typically linked to 'FaultB Flag' in the image. If multiple texts are linked, select only the one with the clearest functional relevance, avoiding secondary annotations.\n" +
                "\n" +
                "4. **Ensure precision**: Extract text exactly as it appears in the image, without modification, guessing, or inference. Ensure all identified identifiers, including 'DX001', are included in the output.\n" +
                "\n" +
                "Return the result as a JSON array, where each object contains the following fields:\n" +
                "- `\"id\"`: A string representing the exact identifier (e.g., 'DI001', 'DX001', 'DI016').\n" +
                "- `\"description\"`: A string representing the associated function name or textual description.\n" +
                "\n" +
                "Example output formats:\n" +
                "```json\n" +
                "[\n" +
                "  {\n" +
                "    \"id\": \"DI001\",\n" +
                "    \"description\": \"Open/Short Detect\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"id\": \"DX001\",\n" +
                "    \"description\": \"FaultB Flag\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"id\": \"DI016\",\n" +
                "    \"description\": \"ECC\"\n" +
                "  }\n" +
                "]\n" +
                "```\n" +
                "```json\n" +
                "[\n" +
                "  {\n" +
                "    \"id\": \"DI010\",\n" +
                "    \"description\": \"Thermal Shutdown\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"id\": \"DI011\",\n" +
                "    \"description\": \"ADC\"\n" +
                "  }\n" +
                "]\n" +
                "```\n" +
                "```json\n" +
                "[\n" +
                "  {\n" +
                "    \"id\": \"DI003\",\n" +
                "    \"description\": \"PWM Loop Duty Test\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"id\": \"DI004\",\n" +
                "    \"description\": \"PWM Loop Duty Injection Test\"\n" +
                "  }\n" +
                "]\n" +
                "```\n" +
                "\n" +
                "Do not return anything other than the JSON array. Focus solely on identifiers that start with the letter 'D'.";
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

    private String searchVectorDb(String imageAnalysisResult) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        List<RuleTxt> ruleTxtList = objectMapper.readValue(imageAnalysisResult, new TypeReference<List<RuleTxt>>() {
        });
        StringBuilder sb = new StringBuilder();
        List<String> texts = new ArrayList<>();
        for (RuleTxt item : ruleTxtList) {
            String ruleDesc = search(item);
            texts.add(ruleDesc);
        }
        texts= texts.stream()
                .filter(Objects::nonNull)
                .filter(s -> !s.trim().isEmpty())
                .sorted()
                .distinct()
                .collect(Collectors.toList());
        for (String text : texts) {
            sb.append(text);
        }
        if (sb.length() == 0) {
            return null;
        }
        return sb.toString();
    }

    private String search(RuleTxt ruleTxt) {
        String query = ruleTxt.getDescription();
        if (query.trim().isEmpty()) {
            return "";
        }
        String category = "lumissil-dx";
        Map<String, String> metadatas = new HashMap<>();
        metadatas.put("category", category);
        List<List<IndexSearchData>> searchResults = vectorStoreService.search(query, 1, 0.5, metadatas, category, 1, 1);
        StringBuilder sb = new StringBuilder();
        if (!searchResults.isEmpty()) {
            List<IndexSearchData> firstResult = searchResults.get(0);
            if (firstResult.size() != 3) {
                return "";
            }
            sb.append(ruleTxt.getId()).append(" ")
                    .append(firstResult.get(1).getText()).append("\n")
                    .append(firstResult.get(2).getText()).append("\n\n");
            return sb.toString();
        }
        return "";
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

        public String getImage_url() {
            return image_url;
        }

        public void setImage_url(String image_url) {
            this.image_url = image_url;
        }

        public String getPrompt() {
            return prompt;
        }

        public void setPrompt(String prompt) {
            this.prompt = prompt;
        }
    }

    // Response class for vision model
    private static class VisionResponse {
        private String result;

        public String getResult() {
            return result;
        }

        public void setResult(String result) {
            this.result = result;
        }
    }
}
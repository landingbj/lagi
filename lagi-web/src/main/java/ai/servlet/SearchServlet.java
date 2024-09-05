package ai.servlet;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;

import ai.common.pojo.*;
import ai.intent.impl.SampleIntentServiceImpl;
import ai.intent.pojo.IntentResult;
import ai.vector.VectorDbService;
import ai.servlet.annotation.Body;
import ai.servlet.annotation.Post;
import ai.utils.*;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
import okhttp3.sse.EventSources;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import ai.common.client.AiServiceCall;
import ai.common.client.AiServiceInfo;
import ai.migrate.service.AudioService;
import ai.llm.service.CompletionsService;
import ai.learn.questionAnswer.KShingleFilter;
import ai.migrate.service.ApiService;
import ai.migrate.service.IntentService;
import ai.openai.pojo.ChatCompletionRequest;
import ai.openai.pojo.ChatCompletionResult;
import ai.openai.pojo.ChatMessage;
import ai.qa.LLMConfig;
import ai.utils.qa.ChatCompletionUtil;

@MultipartConfig(fileSizeThreshold = 1024 * 1024, maxFileSize = 1024 * 1024 * 5, maxRequestSize = 1024 * 1024 * 5)
public class SearchServlet extends RestfulServlet {
    private static final long serialVersionUID = 1L;
    private ApiService apiService = new ApiService();
    private IntentService intentService = new IntentService();
    private Gson gson = new Gson();

    private static Configuration config = MigrateGlobal.config;
    private AudioService audioService = new AudioService();
    private CompletionsService completionsService = new CompletionsService();

    private VectorDbService vectorDbService = new VectorDbService(config);
    private ai.intent.IntentService sampleIntentService = new SampleIntentServiceImpl();


    @Post("generateExam")
    public void generateExam(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        resp.setHeader("Content-Type", "application/json;charset=utf-8");
        String jsonString = IOUtils.toString(req.getInputStream(),
                StandardCharsets.UTF_8);
        ObjectMapper objectMapper = new ObjectMapper();
        String result = "[]";
        try {
            JsonNode jsonNode = objectMapper.readTree(jsonString);
            String content = jsonNode.get("content").asText();
            Object[] params = {content, "temp", "ln_unicom"};
            AiServiceCall wsCall = new AiServiceCall();
            String returnStr = wsCall.callWS(AiServiceInfo.WSLrnUrl,
                    "getInstructions", params)[0];
            JsonNode jsonNode1 = objectMapper.readTree(returnStr);
            ArrayList<ArrayList<String>> list = new ArrayList<>();
            for (JsonNode node : jsonNode1) {
                String instruction = node.get("instruction").asText();
                String input = node.get("input").asText();
                String output = node.get("output").asText();
                ArrayList<String> inter = new ArrayList<>();
                inter.add(instruction);
                inter.add(output);
                list.add(inter);
            }
            result = objectMapper.writeValueAsString(list);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } finally {
            PrintWriter out = resp.getWriter();
            out.print(result);
            out.flush();
            out.close();
        }

        // String jsonString = IOUtils.toString(req.getInputStream(),

    }

    @Post("uploadFile")
    public void uploadFile(HttpServletRequest request,
                           HttpServletResponse response) throws ServletException, IOException {
        String allowedOrigin = "https://localhost";
        response.setCharacterEncoding("UTF-8");
        Part filePart = request.getPart("file"); // 与前端发送的FormData中的字段名对应
        String fileName = getFileName1(filePart);
        String tempFolder;
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            tempFolder = "C:\\temp\\"; // Windows系统下的临时文件夹
        } else if (os.contains("nix") || os.contains("nux")
                || os.contains("mac")) {
            tempFolder = "/tmp/"; // Unix或Mac系统下的临时文件夹
        } else {
            tempFolder = "/var/tmp/"; // 默认的临时文件夹
        }
        File tempDir = new File(tempFolder);
        if (!tempDir.exists()) {
            tempDir.mkdirs(); // 创建临时文件夹及其父文件夹（如果不存在）
        }

        String UPLOAD_DIRECTORY = tempFolder;
        // 检查文件名是否为空
        if (fileName != null && !fileName.isEmpty()) {
            try {
                // 将文件保存到指定目录
                String filePath = UPLOAD_DIRECTORY + File.separator + fileName;
                File file = new File(filePath);
                try (InputStream input = filePart.getInputStream();
                     OutputStream output = new FileOutputStream(file)) {
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = input.read(buffer)) > 0) {
                        output.write(buffer, 0, length);
                    }
                }

                // 成功上传的响应
                JSONObject jsonResponse = new JSONObject();
                jsonResponse.put("message", "文件上传成功");
                response.getWriter().write(jsonResponse.toString());
                // 请接口，调用接口
                uploadFileToAPI(filePath, fileName);

            } catch (Exception e) {
                // 上传失败的响应
                JSONObject jsonResponse = new JSONObject();
                jsonResponse.put("error", "文件上传失败");
                response.getWriter().write(jsonResponse.toString());
            }
        } else {
            // 文件名为空的响应
            JSONObject jsonResponse = new JSONObject();
            jsonResponse.put("error", "没有选择文件");
            response.getWriter().write(jsonResponse.toString());
        }
    }

    private void uploadFileToAPI(String filePath, String fileName) {
    }

    private String getFileName1(Part part) {
        String contentDisposition = part.getHeader("content-disposition");
        String[] tokens = contentDisposition.split(";");
        for (String token : tokens) {
            if (token.trim().startsWith("filename")) {
                return token.substring(token.indexOf('=') + 1).trim()
                        .replace("\"", "");
            }
        }
        return null;
    }

    private String getFileName(Part part) {
        for (String content : part.getHeader("content-disposition").split(";")) {
            if (content.trim().startsWith("filename")) {
                return content.substring(content.indexOf('=') + 1).trim()
                        .replace("\"", "");
            }
        }
        return null;
    }

    @Post("detectIntent")
    public void detectIntent(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        resp.setHeader("Content-Type", "application/json;charset=utf-8");
        HttpSession session = req.getSession();

        String jsonString = IOUtils.toString(req.getInputStream(),
                StandardCharsets.UTF_8);
        QuestionAnswerRequest qaRequest = gson.fromJson(jsonString,
                QuestionAnswerRequest.class);
        String category = qaRequest.getCategory();
        List<ChatMessage> messages = qaRequest.getMessages();

        ObjectMapper objectMapper = new ObjectMapper();
        String content = messages.get(messages.size() - 1).getContent().trim();
//        String intent = intentService.detectIntent(content);
        ChatCompletionRequest request = new ChatCompletionRequest();
        request.setMax_tokens(0);
        request.setModel("");
        request.setTemperature(0);
        request.setMessages(messages);
        IntentResult intentResult = sampleIntentService.detectIntent(request);
        String intent = intentResult.getType();
        PrintWriter out = resp.getWriter();

        String result = "{\"status\":\"failed\"}";

        String lastImagePath = (String) session.getAttribute("last_image_file");
        String lastVideoPath = (String) session.getAttribute("last_video_file");

        if (intent.equals(MigrateGlobal.INSTRUCTION_TYPE_INSTRUCTION)) {
            // 通过路径获取文件的内容
            String lastFilePath = (String) session.getAttribute("lastFilePath");
            // 是否之前有上传的文件
            if (lastFilePath != null && !lastFilePath.equals("")) {
                // 获取内容
                String fileContent = FileAndDocUtil.getContent(lastFilePath);
                // 获取指令集
                ArrayList<InstructionsResponse> instructionsResponseList = FileAndDocUtil
                        .getInstructionsByContent(fileContent);
                Map<String, Object> map = new HashMap<String, Object>();
                map.put("status", "success");
                map.put("instructions", instructionsResponseList);
                result = objectMapper.writeValueAsString(map);
            }
        } else if (intent.equals(MigrateGlobal.INSTRUCTION_TYPE_IMAGE)) {
            result = apiService.generateImage(content, req);
        } else if (intent.equals(MigrateGlobal.INSTRUCTION_TYPE_IMAGE_TO_TEXT) && lastImagePath != null) {
            result = apiService.imageToText(lastImagePath, req);
        } else if (intent.equals(MigrateGlobal.INSTRUCTION_TYPE_ESRGAN) && lastImagePath != null) {
            result = apiService.enhanceImage(lastImagePath, req);
        } else if (intent.equals(MigrateGlobal.INSTRUCTION_TYPE_SVD) && lastImagePath != null) {
            result = apiService.generateVideo(lastImagePath, req);
        } else if (intent.equals(MigrateGlobal.INSTRUCTION_TYPE_SVD_BY_TEXT)) {
            result = apiService.generateVideoByText(content, req);
        } else if (intent.equals(MigrateGlobal.INSTRUCTION_TYPE_MMTRACKING) && lastVideoPath != null) {
            result = apiService.motInference(lastVideoPath, req);
        } else if (intent.equals(MigrateGlobal.INSTRUCTION_TYPE_MMEDITING) && lastVideoPath != null) {
            result = apiService.mmeditingInference(lastVideoPath, req);
        } else {
            Map<String, Object> map = new HashMap<>();
            map.put("status", "success");
            map.put("data", MigrateGlobal.INSTRUCTION_TYPE_TEXT);
            result = gson.toJson(map);
        }
        out.print(result);
        out.flush();
        out.close();
    }

    @Post("questionAnswer")
    public void questionAnswer(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        HttpSession session = req.getSession();

        String jsonString = IOUtils.toString(req.getInputStream(),
                StandardCharsets.UTF_8);
        QuestionAnswerRequest qaRequest = gson.fromJson(jsonString, QuestionAnswerRequest.class);
        String category = qaRequest.getCategory();
        ChatSession chatSession = callQuestionAnswer(qaRequest, category, session);
        LinkedBlockingQueue<String> queue = new LinkedBlockingQueue<>();
        if (qaRequest.getStream() == null || !qaRequest.getStream()) {
            resp.setHeader("Content-Type", "application/json;charset=utf-8");
            PrintWriter out = resp.getWriter();
            ChatResponseWithContext responseWithContext = completions(chatSession, req);
            List<ChatResponseWithContext> responseList = new ArrayList<>();
            Map<String, Object> map = new HashMap<>();
            if (responseWithContext == null) {
                map.put("status", "failed");
            } else {
                responseList.add(responseWithContext);
                map.put("status", "success");
                map.put("data", responseList);
            }
            out.print(gson.toJson(map));
            out.flush();
            out.close();
        } else {
            streamCompletions(chatSession, queue, req);
            resp.setHeader("Content-Type", "text/event-stream;charset=utf-8");
            PrintWriter out = resp.getWriter();
            String partResult;
            try {
                while (!(partResult = queue.take()).equals("[CLOSED]")) {
                    out.print("data: " + partResult + "\n\n");
                    out.flush();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            out.flush();
            out.close();
        }
    }


    private ChatResponseWithContext completions(ChatSession chatSession, HttpServletRequest req) {
        ChatCompletionRequest chatCompletionRequest = chatSession.getChatCompletionRequest();
        ChatResponseWithContext responseWithContext = chatSession.getChatResponseWithContext();
        List<String> imageList = responseWithContext.getImageList();
        if (chatCompletionRequest.getStream() == null || !chatCompletionRequest.getStream()) {
            ChatCompletionResult chatCompletionResult = completionsService.completions(chatCompletionRequest);
            if (chatCompletionResult == null) {
                return null;
            }
            String text = chatCompletionResult.getChoices().get(0).getMessage().getContent();
            responseWithContext.setText(text);
            responseWithContext.setImageList(getImageFiles(imageList, req));
        }
        return responseWithContext;
    }

    private List<String> getImageFiles(List<String> imageList, HttpServletRequest req) {
        String uri = req.getScheme() + "://" + req.getServerName() + ":" + req.getServerPort();
        ArrayList<String> result = null;
        if (imageList != null) {
            result = new ArrayList<>();
            for (String url : imageList) {
                ServletContext context = req.getServletContext();
                String rootPath = context.getRealPath("");
                String filePath = rootPath + "static/img/imgList/";
                File tempDir = new File(filePath);
                if (!tempDir.exists()) {
                    tempDir.mkdirs();
                }
                WhisperResponse whisperResponse1 = DownloadUtils.downloadFile(url, "png", filePath);
                String urlResult = uri + "/static/img/imgList/" + whisperResponse1.getMsg();
                result.add(urlResult);
            }
        }
        return result;
    }

    private void streamCompletions(ChatSession chatSession, Queue<String> queue, HttpServletRequest req) {
        String API_URL = "http://116.255.226.214:8090/v1/chat/completions";

        ChatCompletionRequest chatCompletionRequest = chatSession.getChatCompletionRequest();
        chatCompletionRequest.setModel("/mnt/data/vicuna-13b-v1.5-16k");
        chatCompletionRequest.setStream(true);

        ChatResponseWithContext responseWithContext = chatSession.getChatResponseWithContext();
        List<String> imageList = responseWithContext.getImageList();

        OkHttpClient client = new OkHttpClient();

        String context = responseWithContext.getContext();
        responseWithContext.setImageList(null);
        responseWithContext.setContext(null);

        System.out.println(gson.toJson(chatCompletionRequest));
//        PrintWriter out = resp.getWriter();

        Gson gson = new Gson();
        Request request = new Request.Builder()
                .url(API_URL)
//                .header("Authorization", "Bearer " + API_KEY)
                .header("Accept", "text/event-stream")
                .post(RequestBody.create(gson.toJson(chatCompletionRequest), MediaType.parse("application/json")))
                .build();


        EventSource.Factory factory = EventSources.createFactory(client);

        EventSource eventSource = factory.newEventSource(request, new EventSourceListener() {
            String allContent = "";
            String tempChunk = "";


            @Override
            public void onOpen(@NotNull EventSource eventSource, @NotNull Response response) {
                System.out.println("SSE opened.");
            }

            @Override
            public void onEvent(@NotNull EventSource eventSource, @Nullable String id, @Nullable String type, @NotNull String data) {
//                System.out.println("Received event: " + data);
                if (!data.equals("[DONE]")) {
                    ChatCompletionResult chatCompletionResult = gson.fromJson(data, ChatCompletionResult.class);
                    SensitiveWordUtil.filter(chatCompletionResult);
                    chatCompletionResult.getChoices().get(0).getDelta().getContent();
                    String chunk = chatCompletionResult.getChoices().get(0).getDelta().getContent();
                    if (chunk != null) {
                        tempChunk += chunk;
                        allContent += chunk;
                    }
                    if (tempChunk.getBytes(StandardCharsets.UTF_8).length >= 2) {
                        responseWithContext.setText(tempChunk);
                        queue.add(gson.toJson(responseWithContext));
                        tempChunk = "";
                        responseWithContext.setText(tempChunk);
                    }
                } else {
                    if (!tempChunk.isEmpty()) {
                        responseWithContext.setText(tempChunk);
                    }
                    responseWithContext.setImageList(getImageFiles(imageList, req));
                    if (context != null) {
                        responseWithContext.setContext(context);
                    }
                    queue.add(gson.toJson(responseWithContext));
                    queue.add("[DONE]");
                }
            }

            @Override
            public void onFailure(@NotNull EventSource eventSource, @Nullable Throwable t, @Nullable Response response) {
                System.err.println("SSE failed." + response);
                if (t != null) {
                    t.printStackTrace();
                }
                closeConnection(eventSource);
                queue.add("[CLOSED]");
            }

            @Override
            public void onClosed(@NotNull EventSource eventSource) {
                System.out.println("SSE closed.");
                closeConnection(eventSource);
                queue.add("[CLOSED]");
            }

            private void closeConnection(EventSource eventSource) {
                eventSource.cancel();
                client.dispatcher().executorService().shutdown();
            }
        });
    }

    private ChatSession getResponseList(List<ChatMessage> messages, String category, HttpSession session) {
        ChatCompletionRequest request = new ChatCompletionRequest();
        request.setCategory(category);
        request.setMessages(messages);
        request.setTemperature(LLMConfig.TEMPERATURE);
        request.setMax_tokens(LLMConfig.LLM_MAX_TOKENS);
        String lastMessage = ChatCompletionUtil.getLastMessage(request);
        List<IndexSearchData> indexSearchDataList = null;
        if (vectorDbService.vectorStoreEnabled()) {
            indexSearchDataList = vectorDbService.search(lastMessage, category);
        }
        if (indexSearchDataList != null && !indexSearchDataList.isEmpty()) {
            String contextText = indexSearchDataList.get(0).getText();
            String prompt = ChatCompletionUtil.getPrompt(contextText, lastMessage);
            ChatCompletionUtil.setLastMessage(request, prompt);
        }
        ChatResponseWithContext chatResponseWithContext = new ChatResponseWithContext();
        if (indexSearchDataList != null && !indexSearchDataList.isEmpty()) {
            IndexSearchData indexSearchData = indexSearchDataList.get(0);
            chatResponseWithContext.setContext(indexSearchData.getText());
            chatResponseWithContext.setCategory(category);
            chatResponseWithContext.setDistance(indexSearchData.getDistance());
            chatResponseWithContext.setImage(indexSearchData.getImage());
            chatResponseWithContext.setFilename(indexSearchData.getFilename());
            chatResponseWithContext.setFilepath(indexSearchData.getFilepath());
            chatResponseWithContext.setIndexId(indexSearchData.getId());
        }
        ChatSession chatSession = new ChatSession();
        chatSession.setChatCompletionRequest(request);
        chatSession.setChatResponseWithContext(chatResponseWithContext);
        return chatSession;
    }

    private ChatSession callQuestionAnswer(QuestionAnswerRequest qaRequest, String category, HttpSession session) throws IOException {
        List<ChatMessage> messages = qaRequest.getMessages();
        ChatSession chatSession = getResponseList(messages, category, session);
        String question = messages.get(messages.size() - 1).getContent();
        ChatResponseWithContext response = chatSession.getChatResponseWithContext();
        if (response != null) {
            double threshold = 0.33d;
            double frequencyThreshold = 0.5;
            if (question.length() <= 8) {
                threshold = 0.4d;
                frequencyThreshold = 0.6;
            }
            KShingleFilter kShingleFilter = new KShingleFilter(question.length() - 1, threshold, frequencyThreshold);
            String context = response.getContext();
            if (context != null && !kShingleFilter.isSimilar(question, context)) {
                response.setIndexId(null);
                response.setFilename(null);
                response.setFilepath(null);
                response.setAuthor(null);
                response.setDistance(null);
                response.setContext(null);
            }

            if (response.getImage() != null && !response.getImage().isEmpty()) {
                List<JsonObject> imageObjectList = gson.fromJson(response.getImage(), new TypeToken<List<JsonObject>>() {
                }.getType());
                List<String> imageList = new ArrayList<>();
                for (JsonObject image : imageObjectList) {
                    String url = MigrateGlobal.FILE_PROCESS_URL + "/static/" + image.get("path").getAsString();
                    imageList.add(url);
                }
                response.setImageList(imageList);
            }

        }
        return chatSession;
    }

    @Post("intentDetect")
    public String intentDetect(@Body QuestionAnswerRequest qaRequest) {
        List<ChatMessage> messages = qaRequest.getMessages();
        ChatCompletionRequest request = new ChatCompletionRequest();
        request.setMessages(messages);
        request.setMax_tokens(0);
        request.setModel("");
        request.setTemperature(0);
        IntentResult intentResult = sampleIntentService.detectIntent(request);
        return intentResult.getType();
    }
}

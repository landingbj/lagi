package ai.agent.service;

import ai.agent.AgentFactory;
import ai.agent.AgentGlobal;
import ai.agent.pojo.*;
import ai.agent.social.SocialAgent;
import ai.llm.service.CompletionsService;
import ai.openai.pojo.ChatCompletionRequest;
import ai.openai.pojo.ChatCompletionResult;
import ai.openai.pojo.ChatMessage;
import ai.utils.LagiGlobal;
import ai.utils.OkHttpUtil;
import ai.worker.DefaultWorker;
import ai.worker.Worker;
import ai.worker.pojo.WorkData;
import ai.worker.social.RobotWorker;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RpaService {
    private static final String SAAS_URL = AgentGlobal.SAAS_URL;
    private static final Gson gson = new Gson();
    private static final String patternString = "\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}";
    private static final Pattern pattern = Pattern.compile(patternString);
    private static final Logger log = LoggerFactory.getLogger(RpaService.class);
//    private DefaultWorker defaultWorker = new DefaultWorker();

    private final CompletionsService completionsService = new CompletionsService();

    public StatusResponse getAuthStatus(String appId, String username) {
        String url = SAAS_URL + "/" + "getAuthStatus";
        return getStatusResponse(url, appId, username);
    }

    public RpaResponse getLoginStatus(String appId, String username) {
        String url = SAAS_URL + "/saas/" + "getLoginStatus";
        Map<String, String> params = new HashMap<>();
        params.put("appId", appId);
        params.put("username", username);
        String json;
        try {
            json = OkHttpUtil.get(url, params);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return gson.fromJson(json, RpaResponse.class);
    }

    private StatusResponse getStatusResponse(String url, String appId, String username) {
        Map<String, String> params = new HashMap<>();
        params.put("appId", appId);
        params.put("username", username);
        String json;
        try {
            json = OkHttpUtil.get(url, params);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return gson.fromJson(json, StatusResponse.class);
    }

    public GetLoginQrCodeResponse getLoginQrCode(String appId, String username) {
        String url = SAAS_URL + "/" + "getLoginQrCode";
        Map<String, String> params = new HashMap<>();
        params.put("appId", appId);
//        params.put("username", "女儿红");
        params.put("username", username);
        String json;
        try {
            json = OkHttpUtil.get(url, params);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return gson.fromJson(json, GetLoginQrCodeResponse.class);
    }

    public RpaResponse closeAccount(String username) {
        String url = SAAS_URL + "/saas/closeAccount";
        Map<String, String> params = new HashMap<>();
        params.put("username", username);
        String json = null;
        try {
            json = OkHttpUtil.post(url, gson.toJson(params));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return gson.fromJson(json, RpaResponse.class);
    }

    public GetAppListResponse getAppTypeList() {
        String url = SAAS_URL + "/" + "getAppTypeListV2";
        Map<String, String> params = new HashMap<>();
        String json;
        try {
            json = OkHttpUtil.get(url, params);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        GetAppListResponse response = gson.fromJson(json, GetAppListResponse.class);
        return response;
    }

    public ChannelUserResponse getChannelUser() {
        String url = SAAS_URL + "/" + "getChannelUser";
        Map<String, String> params = new HashMap<>();
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + LagiGlobal.getAgentApiKey());
        String json;
        try {
            json = OkHttpUtil.get(url, params, headers);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return gson.fromJson(json, ChannelUserResponse.class);
    }

    public String getStandardTime(String prompt) {
        ChatCompletionRequest chatCompletionRequest = getTimeCompletionRequest(prompt);
        ChatCompletionResult result = completionsService.completions(chatCompletionRequest);
        if (!result.getChoices().isEmpty()) {
            return extractDatetime(result.getChoices().get(0).getMessage().getContent());
        }
        return null;
    }

    private ChatCompletionRequest getTimeCompletionRequest(String prompt) {
        ChatCompletionRequest chatCompletionRequest = new ChatCompletionRequest();
        chatCompletionRequest.setTemperature(0.8);
        chatCompletionRequest.setStream(false);
        chatCompletionRequest.setMax_tokens(400);
        List<ChatMessage> messages = new ArrayList<>();
        ChatMessage message = new ChatMessage();
        message.setRole("user");
        String date = formatDate(new Date());
        String content = "当前时间是：" + date + "。把下面的文字转换成时间，格式为yyyy-MM-dd HH:mm:ss，例如：2022-01-01 03:10:20，结果只返回时间部分。\n";
        message.setContent(content + prompt);
        messages.add(message);
        chatCompletionRequest.setMessages(messages);
        return chatCompletionRequest;
    }

    public String formatDate(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(date);
    }

    public RpaResponse addTimerTask(AddTimerRequest request) {
        RpaContact contact = RpaContact.builder().
                appId(request.getAppId()).
                channelId(request.getChannelId()).
                contactName(request.getContact()).build();
        AddAppContactResponse addAppContactResponse = addAppContact(contact);
        if (addAppContactResponse.getStatus().equals("failed")) {
            return null;
        }
        Integer contactId = addAppContactResponse.getData().get(0).getId();
        Map<String, String> params = new HashMap<>();
        params.put("appId", request.getAppId().toString());
        params.put("channelId", request.getChannelId().toString());
        params.put("sendTime", formatDate(request.getSendTime()));
        params.put("contactIdList", contactId.toString());
        params.put("message", request.getMessage());
        params.put("repeatFlag", "0");
        params.put("repeatDay", "1");
        params.put("repeatHour", "0");
        params.put("repeatMinute", "0");

        String url = SAAS_URL + "/addRpaTask";
        String json = null;
        try {
            json = OkHttpUtil.postForm(url, params);
        } catch (IOException e) {
            e.printStackTrace();
        }
        RpaResponse response = gson.fromJson(json, RpaResponse.class);
        return response;
    }

    public AddAppContactResponse addAppContact(RpaContact rpaContact) {
        String url = SAAS_URL + "/addAppContact";
        String json = null;
        try {
            json = OkHttpUtil.post(url, gson.toJson(rpaContact));
        } catch (IOException e) {
            e.printStackTrace();
        }
        AddAppContactResponse response = gson.fromJson(json, AddAppContactResponse.class);
        return response;
    }

    public boolean startRobot(StartRobotRequest request) {
        String prompt = request.getPrompt();
        boolean robotEnable = isAffirmative(prompt);
        for (String appId : request.getAppIdList()) {
            SocialAgentParam param = SocialAgentParam.builder()
                    .appId(appId)
                    .username(request.getUsername())
                    .robotFlag(robotEnable ? AgentGlobal.ENABLE_FLAG : AgentGlobal.DISABLE_FLAG)
                    .timerFlag(AgentGlobal.ENABLE_FLAG)
                    .repeaterFlag(AgentGlobal.DISABLE_FLAG)
                    .guideFlag(AgentGlobal.DISABLE_FLAG)
                    .build();

            WorkerThread workerThread = new WorkerThread(param, 1000 * 60 * 30);
            workerThread.start();
        }
        return robotEnable;
    }

    public boolean isAffirmative(String prompt) {
        ChatCompletionRequest chatCompletionRequest = getAffirmativeCompletionRequest(prompt);
        ChatCompletionResult result = completionsService.completions(chatCompletionRequest);
        if (!result.getChoices().isEmpty()) {
            return result.getChoices().get(0).getMessage().getContent().contains("是");
        }
        return false;
    }

    private ChatCompletionRequest getAffirmativeCompletionRequest(String prompt) {
        ChatCompletionRequest chatCompletionRequest = new ChatCompletionRequest();
        chatCompletionRequest.setTemperature(0.8);
        chatCompletionRequest.setStream(false);
        chatCompletionRequest.setMax_tokens(400);
        List<ChatMessage> messages = new ArrayList<>();
        ChatMessage message = new ChatMessage();
        message.setRole("user");
        String content = "下面这段文字是否为肯定的回复？回答只能回答“是”或者“否”。\n";
        message.setContent(content + prompt);
        messages.add(message);
        chatCompletionRequest.setMessages(messages);
        return chatCompletionRequest;
    }

    private String extractDatetime(String text) {
        Matcher matcher = pattern.matcher(text);
        String datetimeStrings = null;
        while (matcher.find()) {
            datetimeStrings = matcher.group();
        }
        return datetimeStrings;
    }

    public static class WorkerThread extends Thread {
        private final long sleepTime;
        private final SocialAgentParam param;

        public WorkerThread(SocialAgentParam param, long sleepTime) {
            this.sleepTime = sleepTime;
            this.param = param;
        }

        @Override
        public void run() {
            SocialAgent agent = AgentFactory.getSocialAgent(param);
            Worker<Boolean, Boolean> worker = new RobotWorker(agent);
            worker.notify(true);
            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                log.error("rpaService sleep error", e);
            }
            worker.notify(false);
        }
    }
}

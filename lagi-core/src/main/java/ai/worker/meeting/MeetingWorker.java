package ai.worker.meeting;

import ai.llm.service.CompletionsService;
import ai.openai.pojo.ChatCompletionRequest;
import ai.openai.pojo.ChatCompletionResult;
import ai.openai.pojo.ChatMessage;
import ai.utils.LagiGlobal;
import ai.worker.pojo.MeetingInfo;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MeetingWorker {
    private static final Pattern jsonPattern = Pattern.compile("\\{[^{}]*}");
    private final CompletionsService completionsService = new CompletionsService();
    private final Gson gson = new Gson();
    private static final String[] MEETING_ADDRESS_ARRAY = {"东四", "西单", "酒仙桥", "洋桥", "大郊亭", "房山", "顺义", "昌平", "大兴", "平谷", "密云", "延庆", "通州", "海淀北", "德胜门", "兆泰"};
    private static final String MEETING_ADDRESS_STR;

    static {
        MEETING_ADDRESS_STR = String.join("、", MEETING_ADDRESS_ARRAY);
    }

    public MeetingInfo extractAddMeetingInfo(String message, MeetingInfo currentMeetingInfo) {
        ChatCompletionRequest chatCompletionRequest = generateAddMeetingRequest(message);
        ChatCompletionResult result = completionsService.completions(chatCompletionRequest);
        String content = result.getChoices().get(0).getMessage().getContent();
        String json = extractJson(content);
        if (json == null) {
            return currentMeetingInfo;
        }
        MeetingInfo meetingInfo = gson.fromJson(json, MeetingInfo.class);
        if (meetingInfo == null) {
            return currentMeetingInfo;
        }
        setEmptyStringToNull(meetingInfo);
        filterMeetingInfo(message, meetingInfo);
        return combineMeetingInfo(currentMeetingInfo, meetingInfo);
    }

    private void setEmptyStringToNull(MeetingInfo meetingInfo) {
        if (meetingInfo.getMeetingAddress() != null && meetingInfo.getMeetingAddress().isEmpty()) {
            meetingInfo.setMeetingAddress(null);
        }
        if (meetingInfo.getDate() != null && meetingInfo.getDate().isEmpty()) {
            meetingInfo.setDate(null);
        }
        if (meetingInfo.getStartTime() != null && meetingInfo.getStartTime().isEmpty()) {
            meetingInfo.setStartTime(null);
        }
        if (meetingInfo.getDuration() != null && meetingInfo.getDuration().isEmpty()) {
            meetingInfo.setDuration(null);
        }
        if (meetingInfo.getAttendance() != null && meetingInfo.getAttendance().isEmpty()) {
            meetingInfo.setAttendance(null);
        }
    }

    private void filterMeetingInfo(String message, MeetingInfo meetingInfo) {
        System.out.println("meetingInfo: " + meetingInfo);
        if (!containsTimeUnit(message) && meetingInfo.getDuration() != null) {
            meetingInfo.setDuration(null);
        }
        if (!containsMeetingAddress(message) && meetingInfo.getMeetingAddress() != null) {
            meetingInfo.setMeetingAddress(null);
        }
    }

    private boolean containsTimeUnit(String message) {
        return message.contains("小时") || message.contains("分钟");
    }

    private boolean containsMeetingAddress(String message) {
        for (String address : MEETING_ADDRESS_ARRAY) {
            if (message.contains(address)) {
                return true;
            }
        }
        return false;
    }

    private MeetingInfo combineMeetingInfo(MeetingInfo currentMeetingInfo, MeetingInfo meetingInfo) {
        if (currentMeetingInfo == null) {
            return meetingInfo;
        }
        if (meetingInfo == null) {
            return currentMeetingInfo;
        }
        if (currentMeetingInfo.getMeetingAddress() != null && meetingInfo.getMeetingAddress() == null) {
            meetingInfo.setMeetingAddress(currentMeetingInfo.getMeetingAddress());
        }
        if (currentMeetingInfo.getDate() != null && meetingInfo.getDate() == null) {
            meetingInfo.setDate(currentMeetingInfo.getDate());
        }
        if (currentMeetingInfo.getStartTime() != null && meetingInfo.getStartTime() == null) {
            meetingInfo.setStartTime(currentMeetingInfo.getStartTime());
        }
        if (currentMeetingInfo.getDuration() != null && meetingInfo.getDuration() == null) {
            meetingInfo.setDuration(currentMeetingInfo.getDuration());
        }
        if (currentMeetingInfo.getAttendance() != null && meetingInfo.getAttendance() == null) {
            meetingInfo.setAttendance(currentMeetingInfo.getAttendance());
        }
        return meetingInfo;
    }

    private ChatCompletionRequest generateAddMeetingRequest(String message) {
        String prompt = "请根据用户的输入提取与预订会议室相关的信息，并将这些数据格式化为一个JSON字符串。如果用户输入的内容不包含某些信息，则将对应字段设置为空值。\n" +
                "\n" +
                "数据字段应包括：\n" +
                "会议地点：meetingAddress\n" +
                "会议开始日期：date，示例值2024-09-25\n" +
                "会议开始时间：startTime，示例值14:00\n" +
                "会议时长：duration，单位为分钟\n" +
                "参会人数：attendance\n" +
                "\n" +
                "参考信息：\n" +
                "当前时间：%s\n" +
                "可选会议地点：%s\n" +
                "\n" +
                "返回的JSON示例：\n" +
                "{\n" +
                "  \"meetingAddress\": \"\",\n" +
                "  \"date\": \"\",\n" +
                "  \"startTime\": \"\",\n" +
                "  \"duration\": \"\",\n" +
                "  \"attendance\": \"\"\n" +
                "}\n" +
                "\n" +
                "注意：\n" +
                "仅根据用户输入提取信息，参考信息仅为背景参考，不要在用户输入中未提及时填充。用户未提供会议开始日期，该字段设为空。用户未提供会议开始时间，该字段设为空。\n" +
                "返回的JSON数据中，仅包含用户输入中的数据。\n" +
                "\n" +
                "你需要处理的用户输入为：“%s”。\n" +
                "\n" +
                "返回的JSON数据应为：";
        prompt = String.format(prompt, getCurrentDateTime(), MEETING_ADDRESS_STR, message);
        return getChatCompletionRequest(prompt);
    }


    private String extractJson(String input) {
        Matcher matcher = jsonPattern.matcher(input);
        if (matcher.find()) {
            return matcher.group();
        } else {
            return null;
        }
    }

    private String getCurrentDateTime() {
        SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        return dateTimeFormat.format(new Date());
    }

    private ChatCompletionRequest getChatCompletionRequest(String prompt) {
        ChatCompletionRequest chatCompletionRequest = new ChatCompletionRequest();
        chatCompletionRequest.setTemperature(0.8);
        chatCompletionRequest.setStream(false);
        chatCompletionRequest.setMax_tokens(400);
        List<ChatMessage> messages = new ArrayList<>();
        ChatMessage message = new ChatMessage();
        message.setRole(LagiGlobal.LLM_ROLE_USER);
        message.setContent(prompt);
        messages.add(message);
        chatCompletionRequest.setMessages(messages);
        return chatCompletionRequest;
    }

    public static void main(String[] args) {
        LagiGlobal.getConfig();
        MeetingWorker meetingWorker = new MeetingWorker();
//        String message = "帮我预定明天下午3点的酒仙桥会议室，会议时长为一个小时,会议有5个人。";
//        String message = "帮我预定明天的酒仙桥会议室，会议时长为一个小时。";
        String message = "我要预定明天会议室5人";
        MeetingInfo currentMeetingInfo = new MeetingInfo();
        currentMeetingInfo.setMeetingAddress("东四");

        MeetingInfo meetingInfo = meetingWorker.extractAddMeetingInfo(message, null);
        System.out.println("meetingInfo: " + meetingInfo);
        System.out.println("meetingInfo: " + new Gson().toJson(meetingInfo));
    }
}

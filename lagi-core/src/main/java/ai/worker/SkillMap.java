package ai.worker;

import ai.agent.Agent;
import ai.common.utils.LRUCache;
import ai.common.utils.ThreadPoolManager;
import ai.llm.service.CompletionsService;
import ai.openai.pojo.ChatCompletionRequest;
import ai.openai.pojo.ChatCompletionResult;
import ai.openai.pojo.ChatMessage;
import ai.utils.qa.ChatCompletionUtil;
import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import lombok.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class SkillMap {


    private static final ExecutorService executorService;

    private static final LRUCache<String, List<AgentIntentScore>> cachedSkillMap = new LRUCache<>(1000, 30L, TimeUnit.DAYS);


    private final Gson gson = new Gson();

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @ToString
    @Builder
    static class AgentIntentScore {
        private String agentName;
        private String intent;
        private Double score;
    }

    private static String intent_prompt_template = "当用户向系统提问时，你需要首先对问题进行自然语言处理（NLP）。通过NLP技术，你的任务是理解用户的意图，识别问题中的关键词，并将问题进行分类。具体步骤如下：\n" +
            "\n" +
            "问题：\n" +
            "{}\n" +
            "\n" +
            "##字段说明:\n" +
            "intent： 用户意图\n" +
            "#执行要求\n" +
            "1.严格遵循字段说明，不得进行任何推测。\n" +
            "2.当某个字段未找到能准确符合字段提取内容时，输出为\"无\"。\n" +
            "3.仅输出 JSON 格式，不要输出任何解释性文字。\n" +
            "4.确保 JSON 结构清晰，字段名称与内容准确对应。";

    private static String score_prompt_template = "你是一个问答分析专家，你需要首先对问题和给出的回答进行自然语言处理（NLP）。通过NLP技术，你的任务是分析问题与回答的相关性并进行打分：\n" +
            "\n" +
            "问题：\n" +
            "{}\n" +
            "回答：\n" +
            "{}\n" +
            "\n" +
            "打分规则：\n" +
            "当回答表示的意思是对无法做出应答或问题和答案没有关系时：给出 -10分，\n" +
            "当问题和回答相关： 请根据问答的完整性进行打分 分数区间0-10\n" +
            "\n" +
            "##字段说明:\n" +
            "score： 问答相关性评分\n" +
            "#执行要求\n" +
            "1.严格遵循字段说明，不得进行任何推测。\n" +
            "2.当某个字段未找到能准确符合字段提取内容时，输出为\"无\"。\n" +
            "3.仅输出 JSON 格式，不要输出任何解释性文字。\n" +
            "4.确保 JSON 结构清晰，字段名称与内容准确对应。";

    private final CompletionsService completionsService= new CompletionsService();

    private ChatCompletionResult callLLm(String prompt, List<List<String>> history, String userMsg) {
        ChatCompletionRequest request = new ChatCompletionRequest();
        List<ChatMessage> chatMessages = new ArrayList<>();
        ChatMessage systemMessage = new ChatMessage();
        systemMessage.setContent(prompt);
        systemMessage.setRole("system");
        chatMessages.add(systemMessage);
        for (int i = 0; i < history.size(); i++) {
            ChatMessage userMessage = new ChatMessage();
            userMessage.setRole("user");
            userMessage.setContent(history.get(i).get(0));

            ChatMessage assistantMessage = new ChatMessage();
            assistantMessage.setRole("assistant");
            assistantMessage.setContent(history.get(i).get(1));

            chatMessages.add(userMessage);
            chatMessages.add(assistantMessage);
        }
        ChatMessage userMessage = new ChatMessage();
        userMessage.setRole("user");
        userMessage.setContent(userMsg);
        chatMessages.add(userMessage);
        request.setMax_tokens(1024);
        request.setTemperature(0);
        request.setMessages(chatMessages);
        System.out.println("request = " + gson.toJson(request));
        return completionsService.completions(request);
    }



    static {
        ThreadPoolManager.registerExecutor("skill-map");
        executorService= ThreadPoolManager.getExecutor("skill-map");
    }

    public List<Agent<ChatCompletionRequest, ChatCompletionResult>> getAgentByIntent(List<Agent<ChatCompletionRequest, ChatCompletionResult>> agentList, String question, Double edge) {
        String intent = intentDetect(question);
        List<AgentIntentScore> agentIntentScoreByIntent = getAgentIntentScoreByIntent(intent);
        if(agentIntentScoreByIntent == null || agentIntentScoreByIntent.isEmpty()) {
            return agentList;
        }
        Map<String, AgentIntentScore> map = agentIntentScoreByIntent.stream().collect(Collectors.toMap(AgentIntentScore::getAgentName, a -> a));
        List<Agent<ChatCompletionRequest, ChatCompletionResult>> res = agentList.stream().filter(agent -> {
            AgentIntentScore agentIntentScore = map.get(agent.getAgentName());
            if (agentIntentScore == null) {
                return false;
            }
            return agentIntentScore.getScore() > edge;
        }).sorted((o1, o2) -> {
            AgentIntentScore s1 = map.get(o1.getAgentName());
            AgentIntentScore s2 = map.get(o2.getAgentName());
            return s2.getScore().compareTo(s1.getScore());
        }).collect(Collectors.toList());
        if(res.isEmpty()) {
            return agentList;
        }
        return res;
    }

    private List<AgentIntentScore> getAgentIntentScoreByIntent(String intent) {
        // TODO 2024/12/16 query from sqlite
        return cachedSkillMap.get(intent);
    }

    public void updateOrInsert(String agentName, String question, String answer, Double score) {
        final Double s = score;
        executorService.submit(()->{
            String intent = intentDetect(question);
            List<AgentIntentScore> agentIntentScores = cachedSkillMap.get(intent);

            if(agentIntentScores == null || agentIntentScores.isEmpty()) {
                save(agentName, question, answer, s, intent);
            } else {
                boolean present = agentIntentScores.stream().anyMatch(a -> a.getAgentName().equals(agentName));
                if(!present) {
                    save(agentName, question, answer, s, intent);
                }
            }
        });
    }

    private void save(String agentName, String question, String answer, Double s, String intent) {
        Double scoreTemp = s;
        if(scoreTemp == null) {
            scoreTemp = scoring(question, answer);
        }
        AgentIntentScore agentScore = AgentIntentScore.builder().agentName(agentName).score(scoreTemp).build();
        cachedAgentScore(intent, agentScore);
        // TODO 2024/12/16 save to sqlite
    }

    private void cachedAgentScore(String intent, AgentIntentScore agentScore) {
        synchronized (intent) {
            List<AgentIntentScore> agentScores = cachedSkillMap.get(intent);
            if(agentScores != null) {
                agentScores.add(agentScore);
            } else {
                cachedSkillMap.put(intent, Lists.newArrayList(agentScore));
            }
        }
    }

    public String intentDetect(String question) {
        ChatCompletionResult chatCompletionResult = callLLm(StrUtil.format(intent_prompt_template, question), Collections.emptyList(), "请告诉用户的意图是什么");
        IntentResponse intentResponse = gson.fromJson(ChatCompletionUtil.getFirstAnswer(chatCompletionResult), IntentResponse.class);
        return intentResponse.getIntent();
    }


    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @ToString
    @Builder
    static
    class ScoreResponse {
        private Double score;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @ToString
    @Builder
    static
    class IntentResponse {
        private String intent;
    }

    public Double scoring(String question, String answer) {
        ChatCompletionResult chatCompletionResult = callLLm(StrUtil.format(score_prompt_template, question, answer), Collections.emptyList(), "请给出问题和答案的相关性评分");
        String firstAnswer = ChatCompletionUtil.getFirstAnswer(chatCompletionResult);
        System.out.println(firstAnswer);
        ScoreResponse scoreResponse = gson.fromJson(firstAnswer, ScoreResponse.class);
        return scoreResponse.getScore();
    }



}

package ai.worker;

import ai.agent.Agent;
import ai.common.utils.LRUCache;
import ai.common.utils.ThreadPoolManager;
import ai.config.pojo.AgentConfig;
import ai.learn.questionAnswer.KShingle;
import ai.llm.service.CompletionsService;
import ai.openai.pojo.ChatCompletionRequest;
import ai.openai.pojo.ChatCompletionResult;
import ai.openai.pojo.ChatMessage;
import ai.utils.qa.ChatCompletionUtil;
import ai.worker.pojo.AgentIntentScore;
import ai.worker.pojo.IntentResponse;
import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
public class SkillMap {


    private static final ExecutorService executorService;

    private static final LRUCache<String, List<AgentIntentScore>> cachedSkillMap = new LRUCache<>(1000, 30L, TimeUnit.DAYS);

    private static final ConcurrentHashMap<String, ThreadLocal<Object>> lockMap = new ConcurrentHashMap<>();

    private static final String DB_URL = "jdbc:sqlite:saas.db";

    private static final int maxTry = 3;

    static {
        try {
            Class.forName("org.sqlite.JDBC");
            // 创建数据库连接
            Connection conn = DriverManager.getConnection(DB_URL);
            // 创建表
            String sql = "CREATE TABLE IF NOT EXISTS agent_scores (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "agent_id TEXT NOT NULL," +
                    "agent_name TEXT NOT NULL," +
                    "keyword TEXT NOT NULL," +
                    "question TEXT," +
                    "score REAL NOT NULL," +
                    "UNIQUE (agent_id, keyword)"+
                    ");";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.executeUpdate();
            conn.close();
        } catch (SQLException e) {
            log.error("Error connecting to database", e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static Object getLockObject(String key) {
        return lockMap.computeIfAbsent(key, k -> ThreadLocal.withInitial(Object::new)).get();
    }

    public static void removeLockObject(String key) {
        lockMap.remove(key);
    }

    public void saveToSQLite(String agentId, String agentName, String keyword, String question, Double score) {
        String sql = "INSERT INTO agent_scores(agent_id, agent_name,keyword, question, score) VALUES(?, ?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, agentId);
            pstmt.setString(2, agentName);
            pstmt.setString(3, keyword);
            pstmt.setString(4, question);
            pstmt.setDouble(5, score);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error("Error connecting to database", e);
        }
    }

    private List<AgentIntentScore> getFromSQLite(String keyword) {
        String sql = "SELECT agent_id, agent_name, keyword, question, score FROM agent_scores WHERE keyword = ?";
        List<AgentIntentScore> scores = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, keyword);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                AgentIntentScore score = AgentIntentScore.builder()
                        .agentId(rs.getString("agent_id"))
                        .agentName(rs.getString("agent_name"))
                        .keyword(rs.getString("keyword"))
                        .score(rs.getDouble("score"))
                        .question(rs.getString("question"))
                        .build();
                scores.add(score);
            }
        } catch (SQLException e) {
            log.error("Error connecting to database", e);
        }
        return scores;
    }



    private final Gson gson = new Gson();


    private static String intent_prompt_template = "当用户向系统提问时，你需要首先对问题进行自然语言处理（NLP）。通过NLP技术，你的任务是理解用户的意图，识别问题中的关键词，并将问题进行分类。具体步骤如下：\n" +
            "\n" +
            "问题：\n" +
            "{}\n" +
            "\n" +
            "##字段说明:\n" +
            "keywords 类型：[string] 内容描述：和用户意图有关的关键词列表不包含时间地名人物 \n" +
            "#执行要求\n" +
            "1.严格遵循字段说明，不得进行任何推测。\n" +
            "2.当某个字段未找到能准确符合字段提取内容时，输出为\"无\"。\n" +
            "3.仅输出 JSON 格式，不要输出任何解释性文字。 例如：{\"keywords\":[]}\n" +
            "4.确保输出能被可以由python json.loads解析\n" +
            "5.确保 JSON 结构清晰，字段名称与内容准确对应。";

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
            "3.仅输出 JSON 格式，不要输出任何解释性文字。例如：{\"score\": 0}\n" +
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
        return completionsService.completions(request);
    }



    static {
        ThreadPoolManager.registerExecutor("skill-map");
        executorService= ThreadPoolManager.getExecutor("skill-map");
    }

    public List<Agent<ChatCompletionRequest, ChatCompletionResult>> filterAgentByIntentKeyword(List<Agent<ChatCompletionRequest, ChatCompletionResult>> agentList, String question, Double edge) {
        IntentResponse intentResponse = intentDetect(question);
        List<String> keywords = intentResponse.getKeywords();
        List<AgentIntentScore> agentIntentScoreByIntent = getAgentIntentScoreByIntentKeyword(keywords);
        if(agentIntentScoreByIntent == null || agentIntentScoreByIntent.isEmpty()) {
            return agentList;
        }
        Map<String, AgentIntentScore> map = agentIntentScoreByIntent.stream().collect(Collectors.toMap(AgentIntentScore::getAgentName, a -> a));
        List<Agent<ChatCompletionRequest, ChatCompletionResult>> res = agentList.stream().filter(agent -> {
            AgentIntentScore agentIntentScore = map.get(agent.getAgentConfig().getAppId());
            if (agentIntentScore == null) {
                return false;
            }
            return agentIntentScore.getScore() > edge;
        }).sorted((o1, o2) -> {
            AgentIntentScore s1 = map.get(o1.getAgentConfig().getName());
            AgentIntentScore s2 = map.get(o2.getAgentConfig().getName());
            return s2.getScore().compareTo(s1.getScore());
        }).collect(Collectors.toList());
        if(res.isEmpty()) {
            return agentList;
        }
        return res;
    }

    public List<AgentIntentScore> getAgentIntentScoreByIntentKeyword(List<String> keywords) {
        if(keywords == null || keywords.isEmpty()) {
            return Collections.emptyList();
        }
        List<AgentIntentScore> agentIntentScores = new ArrayList<>();
        for (String keyword : keywords) {
            List<AgentIntentScore> agentIntentScoresByKeyword = cachedSkillMap.get(keyword);
            if (agentIntentScoresByKeyword != null && !agentIntentScoresByKeyword.isEmpty()) {
                agentIntentScores.addAll(agentIntentScoresByKeyword);
            }
        }
        agentIntentScores = combineAgentScore(agentIntentScores, keywords.size());
        if(!agentIntentScores.isEmpty()) {
            return agentIntentScores;
        }
        for(String keyword : keywords){
            List<AgentIntentScore> fromSQLite = getFromSQLite(keyword);
            if (!fromSQLite.isEmpty()) {
                synchronized (getLockObject(keyword)) {
                    List<AgentIntentScore> temp = cachedSkillMap.get(keyword);
                    if(temp == null || temp.isEmpty()) {
                        cachedSkillMap.put(keyword, fromSQLite);
                        agentIntentScores.addAll(fromSQLite);
                    } else {
                        agentIntentScores.addAll(temp);
                    }
                }
                removeLockObject(keyword);
            }
        }
        return combineAgentScore(agentIntentScores, keywords.size());
    }


    private static List<AgentIntentScore> combineAgentScore(List<AgentIntentScore> agentIntentScores, int size) {
        Map<String, AgentIntentScore> agentScoreMap = new HashMap<>();
        for (AgentIntentScore agentIntentScore : agentIntentScores) {
            String agentName = agentIntentScore.getAgentName();
            AgentIntentScore cached = agentScoreMap.get(agentName);
            if(cached == null) {
                agentScoreMap.put(agentName, AgentIntentScore.builder().agentId(agentIntentScore.getAgentId())
                        .agentName(agentIntentScore.getAgentName()).score(agentIntentScore.getScore()).build());
            } else {
                cached.setScore(cached.getScore() + agentIntentScore.getScore());
            }
        }
        agentIntentScores = agentScoreMap.values().stream()
                .peek(a -> a.setScore(a.getScore()/ size))
                .filter(a -> a.getScore() > 6)
                .sorted(Comparator.comparingDouble(AgentIntentScore::getScore).reversed())
                .collect(Collectors.toList());
        return agentIntentScores;
    }


    public void saveAgentScore(AgentConfig agentConfig, String question, String answer) {
        executorService.submit(()->{
            try {
                List<String> keywords = intentDetect(question).getKeywords();
                for (String keyword : keywords) {
                    List<AgentIntentScore> agentIntentScores = cachedSkillMap.get(keyword);
                    if(agentIntentScores == null || agentIntentScores.isEmpty()) {
                        save(agentConfig, question, answer, keyword);
                    } else {
                        boolean present = agentIntentScores.stream().anyMatch(a -> a.getAgentId().equals(agentConfig.getAppId()));
                        if(!present) {
                            save(agentConfig, question, answer, keyword);
                        }
                    }
                }
            } catch (Exception e) {

            }

        });
    }

    private void save(AgentConfig agentConfig, String question, String answer, String keyword) {
        Double score = scoring(question, answer);
        if(score > 0) {
            double similarity = getSimilarity(question, answer);
            score = Math.min(10, score * 0.7  + (similarity * 0.3 * 10)) ;
        }
        AgentIntentScore agentScore = AgentIntentScore.builder()
                .agentId(agentConfig.getAppId()).agentName(agentConfig.getName()).keyword(keyword).score(score)
                .build();
        saveAgentScore(agentScore);
    }

    private void saveAgentScore(AgentIntentScore agentScore) {
        String keyword = agentScore.getKeyword();
        synchronized (getLockObject(keyword)) {
            try {
                List<AgentIntentScore> agentScores = cachedSkillMap.get(keyword);
                if(agentScores != null) {
                    Set<String> set = agentScores.stream().map(AgentIntentScore::getAgentId).collect(Collectors.toSet());
                    if(!set.contains(agentScore.getAgentId())) {
                        agentScores.add(agentScore);
                        saveToSQLite(agentScore.getAgentId(), agentScore.getAgentName(), keyword, agentScore.getQuestion(), agentScore.getScore());
                    }
                } else {
                    cachedSkillMap.put(keyword, Lists.newArrayList(agentScore));
                    saveToSQLite(agentScore.getAgentId(), agentScore.getAgentName(), keyword, agentScore.getQuestion(), agentScore.getScore());
                }
            } catch (Exception ignored) {
            }
        }
        removeLockObject(keyword);
    }

    public double getSimilarity(String question, String answer) {
        KShingle kShingle = new KShingle();
        double[] similarity = kShingle.similarity(question, answer, 2);
        return similarity[0];
    }

    public IntentResponse intentDetect(ChatCompletionRequest chatCompletionRequest) {
        String question = chatCompletionRequest.getMessages().get(chatCompletionRequest.getMessages().size() - 1).getContent();
        return intentDetect(question);
    }

    public IntentResponse intentDetect(String question) {
        for (int i = 0;i < maxTry; i++) {
            try {
                ChatCompletionResult chatCompletionResult = callLLm(StrUtil.format(intent_prompt_template, question), Collections.emptyList(), "请提取一下用户的意图里的关键词");
                return gson.fromJson(ChatCompletionUtil.getFirstAnswer(chatCompletionResult), IntentResponse.class);
            } catch (Exception e) {
            }
        }
        return null;
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

    public Double scoring(String question, String answer) {
        for (int i = 0;i < maxTry; i++) {
            try {
                ChatCompletionResult chatCompletionResult = callLLm(StrUtil.format(score_prompt_template, question, answer), Collections.emptyList(), "请给出问题和答案的相关性评分");
                String firstAnswer = ChatCompletionUtil.getFirstAnswer(chatCompletionResult);
                ScoreResponse scoreResponse = gson.fromJson(firstAnswer, ScoreResponse.class);
                return scoreResponse.getScore();
            } catch (Exception e) {
            }
        }
        return null;
    }


}

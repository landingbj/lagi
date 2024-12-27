package ai.worker;

import ai.agent.Agent;
import ai.config.pojo.WorkerConfig;
import ai.manager.AgentManager;
import ai.openai.pojo.ChatCompletionRequest;
import ai.openai.pojo.ChatCompletionResult;
import ai.router.Route;
import ai.router.Routers;
import ai.router.utils.RouterParser;
import ai.utils.qa.ChatCompletionUtil;


import java.util.*;
import java.util.stream.Collectors;

public class DefaultBestWorker extends Worker<ChatCompletionRequest, ChatCompletionResult>{

    protected List<Agent<ChatCompletionRequest, ChatCompletionResult>> agents = new ArrayList<>();

    protected WorkerConfig workerConfig;

    private final Route route;

    public DefaultBestWorker(WorkerConfig workerConfig) {
        this.workerConfig = workerConfig;
        String ruleName = RouterParser.getRuleName(workerConfig.getRoute());
        this.route = Routers.getInstance().getRoute(ruleName);
        List<String> params = RouterParser.getParams(workerConfig.getRoute());
        if(params.size() == 1 && RouterParser.WILDCARD_STRING.equals(params.get(0))) {
            List<Agent<?, ?>> allAgents = AgentManager.getInstance().agents();
            for (Agent<?, ?> agent : allAgents) {
                String appId = agent.getAgentConfig().getName();
                if(appId != null) {
                    try {
                        agents.add((Agent<ChatCompletionRequest, ChatCompletionResult>) agent);
                    } catch (Exception ignored) {
                    }
                }
            }
        }
        if(params.size() > 1) {
            for (String agentId : params) {
                Agent<ChatCompletionRequest, ChatCompletionResult> agent =
                        (Agent<ChatCompletionRequest, ChatCompletionResult>) AgentManager.getInstance().get(agentId);
                if(agent != null) {
                    agents.add(agent);
                }
            }
        }
    }

    private static String getRuleName(String route) {
        int i = route.indexOf("(");
        return route.substring(0, i);
    }

    private static List<String> getParams(String route) {
        int s = route.indexOf("(");
        int e = route.indexOf(")");
        return Arrays.stream(route.substring(s + 1, e).split(","))
                .map(String::trim)
                .collect(Collectors.toList());
    }


    protected List<Agent<ChatCompletionRequest, ChatCompletionResult>> filterAgentsBySkillMap(List<Agent<ChatCompletionRequest, ChatCompletionResult>> agents, ChatCompletionRequest data) {
        SkillMap skillMap = new SkillMap();
        return skillMap.filterAgentByIntentKeyword(agents, ChatCompletionUtil.getLastMessage(data), 5.0);
    }


    @Override
    public  ChatCompletionResult work(ChatCompletionRequest data){
        ChatCompletionResult result = null;
        if(route == null) {
            return null;
        }
        data.setStream(false);
        List<ChatCompletionResult> results = route.invoke(data, filterAgentsBySkillMap(agents, data));
        if(results != null && !results.isEmpty()) {
            result = results.get(0);
        }
        return result;
    }

    @Override
    public  ChatCompletionResult call(ChatCompletionRequest data){
        return null;
    }

    @Override
    public void notify(ChatCompletionRequest data){
    }
}

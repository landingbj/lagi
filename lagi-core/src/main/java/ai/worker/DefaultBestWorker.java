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
import ai.worker.skillMap.SkillMap;
import cn.hutool.core.bean.BeanUtil;
import lombok.Setter;


import java.util.*;

public class DefaultBestWorker extends RouteWorker{

    protected List<Agent<ChatCompletionRequest, ChatCompletionResult>> agents = new ArrayList<>();

    protected WorkerConfig workerConfig;

    private Route route;

    @Setter
    private List<Agent<ChatCompletionRequest, ChatCompletionResult>> additionalAgents = new ArrayList<>();

    public DefaultBestWorker(WorkerConfig workerConfig) {
        if(workerConfig == null) {
            return;
        }
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
        List<Agent<ChatCompletionRequest, ChatCompletionResult>> all = new ArrayList<>();
        all.addAll(agents);
        all.addAll(additionalAgents);
        ChatCompletionRequest request = new ChatCompletionRequest();
        BeanUtil.copyProperties(data, request);
        List<ChatCompletionResult> results = route.invoke(request, filterAgentsBySkillMap(all, request));
        if(results != null && !results.isEmpty()) {
            result = results.get(0);
        }
        return result;
    }


    @Override
    public DefaultBestWorker clone() throws CloneNotSupportedException {
        DefaultBestWorker cloned = (DefaultBestWorker) super.clone();
        cloned.route = this.route;
        cloned.agents = this.agents;
        cloned.workerConfig = this.workerConfig;
        cloned.additionalAgents = new ArrayList<>();
        return cloned;
    }
}

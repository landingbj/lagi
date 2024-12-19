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
import cn.hutool.core.bean.BeanUtil;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
public class DefaultAppointWorker extends Worker<ChatCompletionRequest, ChatCompletionResult> {

    protected Map<String, Agent<ChatCompletionRequest, ChatCompletionResult>> agentMap = new ConcurrentHashMap<>();

    protected WorkerConfig workerConfig;

    private final Route route;



    public DefaultAppointWorker(WorkerConfig workerConfig) {
        this.workerConfig = workerConfig;
        String routeName = getRuleName(workerConfig.getRoute());
        this.route = Routers.getInstance().getRoute(routeName);
        List<String> agents = getParams(workerConfig.getRoute());
        if(agents.size() == 1 && RouterParser.WILDCARD_STRING.equals(agents.get(0))) {
            List<Agent<?, ?>> allAgents = AgentManager.getInstance().agents();
            for (Agent<?, ?> agent : allAgents) {
                if(agent.getAgentConfig() !=null && agent.getAgentConfig().getAppId() !=null) {
                    try {
                        String appId = agent.getAgentConfig().getAppId();
                        agentMap.put(appId, (Agent<ChatCompletionRequest, ChatCompletionResult>) agent);
                    } catch (Exception ignored) {
                    }
                }
            }
        }
        if(agents.size() > 1) {
            for (String agentId : agents) {
                Agent<ChatCompletionRequest, ChatCompletionResult> agent =
                        (Agent<ChatCompletionRequest, ChatCompletionResult>) AgentManager.getInstance().get(agentId);
                if(agent != null) {
                    agentMap.put(agent.getAgentConfig().getAppId(), agent);
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

    @Override
    public ChatCompletionResult work(ChatCompletionRequest data) {
        return call(data);
    }

    @Override
    public ChatCompletionResult call(ChatCompletionRequest data) {
        String agentId = (String)BeanUtil.getFieldValue(data, "agentId");
        Agent<ChatCompletionRequest, ChatCompletionResult> trAgent = agentMap.get(agentId);
        if(trAgent != null) {
            List<ChatCompletionResult> invoke = route.invoke(data, Lists.newArrayList(trAgent));
            if(invoke != null && !invoke.isEmpty()) {
                ChatCompletionResult communicate = invoke.get(0);
                SkillMap skillMap = new SkillMap();
                skillMap.saveAgentScore(trAgent.getAgentConfig(), ChatCompletionUtil.getLastMessage(data), ChatCompletionUtil.getFirstAnswer(communicate));
                return communicate;
            }
        }
        return null;
    }

    @Override
    public void notify(ChatCompletionRequest data) {

    }
}

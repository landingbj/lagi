package ai.worker;

import ai.agent.Agent;
import ai.config.pojo.WorkerConfig;
import ai.manager.AgentManager;
import ai.openai.pojo.ChatCompletionRequest;
import ai.openai.pojo.ChatCompletionResult;
import ai.router.Routers;
import ai.router.utils.RouterParser;
import ai.utils.qa.ChatCompletionUtil;
import ai.worker.skillMap.SkillMap;
import cn.hutool.core.bean.BeanUtil;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class DefaultAppointWorker extends RouteWorker {

    protected Map<Integer, Agent<ChatCompletionRequest, ChatCompletionResult>> agentMap = new ConcurrentHashMap<>();

    protected WorkerConfig workerConfig;


    public DefaultAppointWorker(WorkerConfig workerConfig) {
        this.workerConfig = workerConfig;
        String routeName = RouterParser.getRuleName(workerConfig.getRoute());
        this.route = Routers.getInstance().getRoute(routeName);
        List<String> agents = RouterParser.getParams(workerConfig.getRoute());
        if(agents.size() == 1 && RouterParser.WILDCARD_STRING.equals(agents.get(0))) {
            List<Agent<?, ?>> allAgents = AgentManager.getInstance().agents();
            for (Agent<?, ?> agent : allAgents) {
                if(agent.getAgentConfig() !=null && agent.getAgentConfig().getAppId() !=null) {
                    try {
                        agentMap.put(agent.getAgentConfig().getId(), (Agent<ChatCompletionRequest, ChatCompletionResult>) agent);
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
                    agentMap.put(agent.getAgentConfig().getId(), agent);
                }
            }
        }
    }


    @Override
    public ChatCompletionResult work(ChatCompletionRequest data) {
        return call(data);
    }

    @Override
    public ChatCompletionResult call(ChatCompletionRequest data) {
        Integer agentId = (Integer)BeanUtil.getFieldValue(data, "agentId");
        Agent<ChatCompletionRequest, ChatCompletionResult> trAgent = agentMap.get(agentId);
        if(trAgent == null) {
            trAgent =  additionalAgents.stream().filter(agent -> agent.getAgentConfig().getId().equals(agentId)).findFirst().orElse(null);
        }
        if(trAgent != null) {
            ChatCompletionRequest request = new ChatCompletionRequest();
            BeanUtil.copyProperties(request, data);
            data.setStream(false);
            List<ChatCompletionResult> invoke = route.invoke(request, Lists.newArrayList(trAgent));
            if(invoke != null && !invoke.isEmpty()) {
                ChatCompletionResult communicate = invoke.get(0);
                try {
                    SkillMap skillMap = new SkillMap();
                    skillMap.saveAgentScore(trAgent.getAgentConfig(), ChatCompletionUtil.getLastMessage(data), ChatCompletionUtil.getFirstAnswer(communicate));
                } catch (Exception e) {

                }
                return communicate;
            }
        }
        return null;
    }


    @Override
    public DefaultAppointWorker clone() throws CloneNotSupportedException {
        DefaultAppointWorker cloned = (DefaultAppointWorker) super.clone();
        cloned.route = this.route;
        cloned.agentMap = this.agentMap;
        cloned.workerConfig = this.workerConfig;
        cloned.additionalAgents = new ArrayList<>();
        return cloned;
    }

}

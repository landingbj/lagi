package ai.worker;

import ai.agent.Agent;
import ai.openai.pojo.ChatCompletionRequest;
import ai.openai.pojo.ChatCompletionResult;
import ai.utils.qa.ChatCompletionUtil;
import cn.hutool.core.bean.BeanUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class DefaultAppointWorker<T, R> extends Worker<T, R> {

    protected Map<String, Agent<T, R>> agentMap = new ConcurrentHashMap<>();

    public DefaultAppointWorker(List<Agent<T, R>> agentList) {
        if(agentList == null) {
            return;
        }
        for (Agent<T, R> agent : agentList){
            if(agent.getAgentConfig() == null || agent.getAgentConfig().getName() == null) {
                continue;
            }
            try {
                agentMap.put(agent.getAgentConfig().getName(), agent);
            } catch (Exception e) {
                log.error("agent register error", e);
            }
        }
    }

    @Override
    public R work(T data) {
        return null;
    }

    @Override
    public R call(T data) {
        String agentId = (String)BeanUtil.getFieldValue(data, "agentId");
        Agent<T, R> trAgent = agentMap.get(agentId);
        if(trAgent != null) {
            R communicate = trAgent.communicate(data);
            if(data instanceof ChatCompletionRequest &&  communicate instanceof ChatCompletionResult) {
                SkillMap skillMap = new SkillMap();
                skillMap.saveAgentScore(trAgent.getAgentConfig(), ChatCompletionUtil.getLastMessage((ChatCompletionRequest)data), ChatCompletionUtil.getFirstAnswer((ChatCompletionResult)communicate));
            }
            return communicate;
        }
        return null;
    }

    @Override
    public void notify(T data) {
        String agentId = (String)BeanUtil.getFieldValue(data, "agentId");
        Agent<T, R> trAgent = agentMap.get(agentId);
        if(trAgent != null) {
            trAgent.send(data);
        }
    }
}

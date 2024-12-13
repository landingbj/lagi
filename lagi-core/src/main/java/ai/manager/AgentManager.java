package ai.manager;

import ai.agent.Agent;
import ai.agent.AgentGlobal;
import ai.agent.pojo.AgentParam;
import ai.agent.pojo.SocialAgentParam;
import ai.config.pojo.AgentConfig;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class AgentManager {
    @Getter
    private static AgentManager instance = new AgentManager();
    private AgentManager(){}
    private final Map<String, Agent<?, ?>> agentsMap = new ConcurrentHashMap<>();
    public void register(List<AgentConfig> agentConfigs)
    {
        agentConfigs.forEach(conf -> {
        String driver = conf.getDriver();
        String name = conf.getName();
        try {
            Class<?> clazz = Class.forName(driver);
            Constructor<?> constructor = clazz.getConstructor(AgentConfig.class);
            Object o = constructor.newInstance(conf);
            agentsMap.put(name, (Agent<?, ?>) o);
        } catch (Exception ignored) {
            try {
                Class<?> clazz = Class.forName(driver);
                Constructor<?> constructor = clazz.getConstructor(AgentParam.class);
                SocialAgentParam param = SocialAgentParam.builder()
                        .username(conf.getApiKey())
                        .robotFlag(AgentGlobal.ENABLE_FLAG)
                        .timerFlag(AgentGlobal.DISABLE_FLAG)
                        .repeaterFlag(AgentGlobal.DISABLE_FLAG)
                        .guideFlag(AgentGlobal.DISABLE_FLAG)
                        .build();
                Object o = constructor.newInstance(param);
                agentsMap.put(conf.getApiKey(), (Agent<?, ?>) o);
            }catch (Exception e) {
                log.error("agent {} register error {}", name, e);
            }
        }
        });
    }

    public  Agent<?, ?> get(String key)
    {
        return agentsMap.getOrDefault(key, null);
    }

    public  List<Agent<?, ?>> agents()
    {
        return new ArrayList<>(agentsMap.values());
    }
}

package ai.worker;

import ai.agent.Agent;
import ai.config.pojo.WorkerConfig;
import ai.openai.pojo.ChatCompletionRequest;
import ai.openai.pojo.ChatCompletionResult;
import ai.router.Route;
import ai.router.Routers;
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
        this.workerConfig = workerConfig;
        String ruleName = workerConfig.getRoute();
        this.route = Routers.getInstance().getRoute(ruleName);
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
        request.setStream(false);
        List<ChatCompletionResult> results = route.invokeAgent(request, all).getResult();
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

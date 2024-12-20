package ai.router;

import ai.agent.Agent;
import ai.openai.pojo.ChatCompletionRequest;
import ai.openai.pojo.ChatCompletionResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.util.Lists;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class PollingRoute extends Route{
    public PollingRoute(String name) {
        super(name);
    }

    private static final AtomicInteger nextServerCyclicCounter = new AtomicInteger(-1);

    private static int incrementAndGetModulo(int modulo) {

        int current;
        int next;
        do {

            current = nextServerCyclicCounter.get();
            next = (current + 1) % modulo;
        } while(!nextServerCyclicCounter.compareAndSet(current, next));

        return next;
    }
    public List<ChatCompletionResult> invoke(ChatCompletionRequest request, List<Agent<ChatCompletionRequest, ChatCompletionResult>> agents) {
        for (int i = 0; i < agents.size(); i++) {
            int index = incrementAndGetModulo(agents.size());
            try {
                return Lists.newArrayList(agents.get(index).communicate(request));
            }catch (Exception e) {
                log.error("polling error", e);
            }
        }
        throw new RuntimeException("polling run error");
    }
}

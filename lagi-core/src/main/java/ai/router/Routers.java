package ai.router;

import ai.common.exception.RRException;
import ai.config.pojo.RouterConfig;
import ai.openai.pojo.ChatCompletionRequest;
import ai.openai.pojo.ChatCompletionResult;
import ai.router.utils.RouterParser;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Routers {

    private static final Logger log = LoggerFactory.getLogger(Routers.class);
    private final Map<String, Route> routes = new ConcurrentHashMap<>();

    @Getter
    private static final Routers instance = new Routers();
    private Routers(){};

    public void register(List<RouterConfig> routerConfigs)
    {
        if(routerConfigs == null || routerConfigs.isEmpty()) {
            return;
        }
        routerConfigs.forEach(conf -> {
            String name = conf.getName();
            String rule = conf.getRule();
            try {
                addRoute(name, RouterParser.parse(name, rule));
            } catch (Exception e) {
                log.error("router {} register error {}", name, e);
            }
        });
    }


    public void addRoute(String rule, Route route) {
        routes.put(rule, route);
    }

    public ChatCompletionResult dispatch(String path, ChatCompletionRequest request) {
        Route route = routes.get(path);
        if (route != null) {
            return route.handle(request);
        }
        throw new RRException(404, "Not Found" + path);
    }
}

package ai.router;

import ai.config.pojo.ModelFunction;
import ai.config.pojo.ModelFunctions;
import ai.config.pojo.RouterConfig;
import ai.config.pojo.WorkerConfig;
import ai.router.utils.RouterParser;
import ai.worker.Worker;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Routers {

    private static final Logger log = LoggerFactory.getLogger(Routers.class);
    private final Map<String, Route> routes = new ConcurrentHashMap<>();

    @Getter
    private static final Routers instance = new Routers();

    private Routers() {
    }

    private void register(String route, RouterConfig ruleConfig) {
        if (ruleConfig == null) {
            return;
        }
        try {
            addRoute(route, RouterParser.parse(ruleConfig.getName(), route));
        } catch (Exception e) {
            log.error("router {} register error {}", route, e);
        }
    }

    public void register(List<WorkerConfig> workerConfigs, List<RouterConfig> routerConfigs) {
        if (workerConfigs == null || workerConfigs.isEmpty()) {
            return;
        }
        workerConfigs.forEach(
                conf -> {
                    String route = conf.getRoute();
                    if (route == null) {
                        route = "pass(%)";
                    }
                    register(route, getRouterConfig(route, routerConfigs));
                }
        );
    }

    public void register(ModelFunctions functions, List<RouterConfig> routerConfigs) {
        register(functions.getChat(), routerConfigs);
    }

    public void register(ModelFunction function, List<RouterConfig> routerConfigs) {
        if (function == null) {
            return;
        }
        register(function.getRoute(), getRouterConfig(function.getRoute(), routerConfigs));
    }

    private RouterConfig getRouterConfig(String route, List<RouterConfig> routerConfigs) {
        String routeName = RouterParser.getRuleName(route);
        for (RouterConfig routerConfig : routerConfigs) {
            if (routerConfig.getName().equals(routeName)) {
                return routerConfig;
            }
        }
        return null;
    }

    public void addRoute(String rule, Route route) {
        routes.put(rule, route);
    }

    public Route getRoute(String rule) {
        return routes.get(rule);
    }

}

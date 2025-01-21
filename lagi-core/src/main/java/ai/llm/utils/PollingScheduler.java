package ai.llm.utils;


import ai.router.Route;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class PollingScheduler {

    private static final AtomicInteger nextServerCyclicCounter = new AtomicInteger(-1);


    private static int incrementAndGetModulo(int modulo) {

        int current;
        int next;
        do {

            current = nextServerCyclicCounter.get();
            next = (current + 1) % modulo;
        } while (!nextServerCyclicCounter.compareAndSet(current, next));

        return next;
    }

    public static String schedule(List<String> servers) {
        if (servers == null || servers.isEmpty()) {
            return null;
        } else {
            return servers.get(incrementAndGetModulo(servers.size()));
        }
    }

    public static Route routeSchedule(List<Route> pollingRoutes) {
        if (pollingRoutes == null || pollingRoutes.isEmpty()) {
            return null;
        } else {
            return pollingRoutes.get(incrementAndGetModulo(pollingRoutes.size()));
        }
    }
}

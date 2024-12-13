package ai.router;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

public class Polling<T,R> implements Function<T, R> {

    private static final Logger log = LoggerFactory.getLogger(Polling.class);
    private final List<Function<T, R>> functions;

    public Polling(List<Function<T, R>> functions) {
        this.functions = functions;
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


    @Override
    public R apply(T t) {
        for (int i = 0; i < functions.size(); i++) {
            int index = incrementAndGetModulo(functions.size());
            try {
                return functions.get(index).apply(t);
            }catch (Exception e) {
                log.error("polling error", e);
            }
        }
        throw new RuntimeException("polling run error");
    }

}

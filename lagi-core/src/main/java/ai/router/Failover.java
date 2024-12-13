package ai.router;

import ai.common.exception.RRException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.function.Function;

public class Failover<T, R> implements Function<T, R>{
    private static final Logger log = LoggerFactory.getLogger(Failover.class);
    private final List<Function<T, R>> functions;

    public Failover(List<Function<T, R>> functions) {
        this.functions = functions;
    }

    @Override
    public R apply(T t) {
        for (Function<T, R> function : functions) {
            try {
                return function.apply(t);
            } catch (Exception e) {
                log.error("polling error", e);
            }
        }
        throw new RRException("failover run error");
    }

}

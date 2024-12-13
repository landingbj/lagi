package ai.router;



import java.util.function.Function;

public class Parallel<T, R> implements Function<T,R> {

    private final Function<T, R> function;

    public Parallel(Function<T, R> function) {
        this.function = function;
    }


    @Override
    public R apply(T t) {
        return function.apply(t);
    }
}

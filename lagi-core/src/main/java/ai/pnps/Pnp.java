package ai.pnps;


import ai.config.pojo.PnpConfig;
import lombok.Data;

@Data
public abstract class Pnp<T, R> implements Cloneable{

    protected PnpConfig pnpConfig;

    public abstract void connect();

    public abstract void terminate();

    public abstract void start();

    public abstract void stop();

    public abstract void send(T request);

    public abstract R receive();

    public abstract R communicate(T data);

    @Override
    public Pnp<T, R> clone() throws CloneNotSupportedException {
        return (Pnp<T, R>) super.clone();
    }
}

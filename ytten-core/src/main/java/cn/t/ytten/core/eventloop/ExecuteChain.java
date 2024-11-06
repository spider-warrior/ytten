package cn.t.ytten.core.eventloop;

import cn.t.ytten.core.exception.UnHandleException;

import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Function;

public class ExecuteChain<T> {
    private final Callable<T> callable;

    public ExecuteChain(Callable<T> callable) {
        this.callable = callable;
    }

    public <R> ExecuteChain<R> map(Function<? super T, ? extends R> function) {
        return new ExecuteChain<>(() -> {
            T value = callable.call();
            return function.apply(value);
        });
    }


    public void execute() {
        execute(t -> {
            throw new UnHandleException(t);
        });
    }

    public void execute(Consumer<Throwable> errorConsumer) {
        try {
            callable.call();
        } catch (Throwable t) {
            errorConsumer.accept(t);
        }
    }
}


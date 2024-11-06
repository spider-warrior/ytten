package cn.t.ytten.core.eventloop;

import cn.t.ytten.core.exception.EventLoopTaskException;

import java.util.function.Consumer;

public class EventLoopTask<V> {
    private final ExecuteChain<V> executeChain;
    private final Consumer<Throwable> errorHandler;
    public void run() {
        try {
            executeChain.execute();
        } catch (Throwable t) {
            if(errorHandler != null) {
                errorHandler.accept(t);
            } else {
                throw new EventLoopTaskException(t);
            }
        }
    }

    public EventLoopTask(ExecuteChain<V> executeChain, Consumer<Throwable> errorHandler) {
        this.executeChain = executeChain;
        this.errorHandler = errorHandler;
    }
}

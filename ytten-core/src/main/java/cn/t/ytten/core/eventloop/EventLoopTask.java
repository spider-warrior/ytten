package cn.t.ytten.core.eventloop;

import cn.t.ytten.core.exception.EventLoopTaskException;

import java.util.concurrent.Callable;
import java.util.function.Consumer;

public class EventLoopTask<V> {
    private Callable<V> callable;
    private Consumer<Throwable> errorHandler;
    private TaskWorkChain<V, ?> taskWorkChain;
    public void run() {
        try {
            V v = callable.call();
            taskWorkChain.process(v);
        } catch (Throwable t) {
            if(errorHandler != null) {
                errorHandler.accept(t);
            } else {
                throw new EventLoopTaskException(t);
            }
        }
    }

    public EventLoopTask(Callable<V> callable, Consumer<Throwable> errorHandler, TaskWorkChain<V, ?> taskWorkChain) {
        this.callable = callable;
        this.errorHandler = errorHandler;
        this.taskWorkChain = taskWorkChain;
    }
}

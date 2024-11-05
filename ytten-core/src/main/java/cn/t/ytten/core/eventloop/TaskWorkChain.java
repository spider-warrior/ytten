package cn.t.ytten.core.eventloop;

import java.util.function.Consumer;
import java.util.function.Function;

public class TaskWorkChain<I, O> {
    private final Function<I, O> fn;
    private Consumer<Throwable> errorConsumer;
    private TaskWorkChain<O, ?> nextNode;

    private TaskWorkChain(Function<I, O> fn) {
        this.fn = fn;
    }

    public void process(I input) {
        try {
            O output = fn.apply(input);
            if (nextNode != null) {
                nextNode.process(output);
            }
        } catch (Throwable t) {
            if(errorConsumer != null) {
                errorConsumer.accept(t);
            } else {
                if(nextNode == null) {
                    throw t;
                } else {
                    nextNode.errorConsumer.accept(t);
                }
            }
        }
    }

    public <OO> TaskWorkChain<O, OO> chain(Function<O, OO> fn) {
        TaskWorkChain<O, OO> nextNode = new TaskWorkChain<>(fn);
        this.nextNode = nextNode;
        return nextNode;
    }

    public void last(Consumer<O> consumer) {
        this.chain(o -> {
            consumer.accept(o);
            return null;
        });
    }

    public void last(Consumer<O> consumer, Consumer<Throwable> errorConsumer) {
        this.last(consumer);
        this.errorConsumer = errorConsumer;
    }

    public static <I> TaskWorkChain<I, I> newInstance() {
        return new TaskWorkChain<>(Function.identity());
    }

    public static void main(String[] args) {
        TaskWorkChain<String, String> chain = newInstance();
        chain.chain(num -> {
            return num + 100;
        }).chain(num -> {
            return num + 200;
        }).chain(num -> {
            return "result: " + num;
        }).chain(input -> {
            System.out.println(input);
            return null;
        });
        chain.process("100");
    }
}

package cn.t.ytten.core.eventloop;

import java.util.function.Function;

public class TaskWorkChain<I, O> {
    private final Function<I, O> fn;
    private TaskWorkChain<O, ?> nextNode;

    private TaskWorkChain(Function<I, O> fn) {
        this.fn = fn;
    }

    public void process(I input) {
        O output = fn.apply(input);
        if (nextNode != null) {
            nextNode.process(output);
        }
    }

    public <OO> TaskWorkChain<O, OO> chain(Function<O, OO> fn) {
        TaskWorkChain<O, OO> nextNode = new TaskWorkChain<>(fn);
        this.nextNode = nextNode;
        return nextNode;
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

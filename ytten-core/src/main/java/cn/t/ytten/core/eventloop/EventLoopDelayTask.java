package cn.t.ytten.core.eventloop;

public class EventLoopDelayTask {
    private final long executeTimePointInMills;
    private final Runnable task;

    public long getExecuteTimePointInMills() {
        return executeTimePointInMills;
    }

    public Runnable getTask() {
        return task;
    }

    public EventLoopDelayTask(long delayInMills, Runnable task) {
        this.executeTimePointInMills = System.currentTimeMillis() + delayInMills;
        this.task = task;
    }
}

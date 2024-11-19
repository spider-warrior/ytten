package cn.t.ytten.core.eventloop;

public class EventLoopDelayTask {
    private long runAt;
    private final boolean repeat;
    private final long rate;
    private final Runnable runnable;

    public long getRunAt() {
        return runAt;
    }

    public void setRunAt(long runAt) {
        this.runAt = runAt;
    }

    public boolean isRepeat() {
        return repeat;
    }

    public long getRate() {
        return rate;
    }

    public Runnable getRunnable() {
        return runnable;
    }

    public EventLoopDelayTask(long rate, Runnable runnable) {
        this(rate,false, runnable);
    }

    public EventLoopDelayTask(long rate, boolean repeat, Runnable runnable) {
        this.rate = rate;
        this.repeat = repeat;
        this.runnable = runnable;
        this.markNextRunAt();
    }

    public void markNextRunAt() {
        this.runAt = System.currentTimeMillis() + this.rate;
    }
}

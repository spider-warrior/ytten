package cn.t.ytten.metricexposer.common.util;

import cn.t.ytten.metricexposer.common.constants.ThreadPoolConfig;
import cn.t.ytten.metricexposer.common.thread.MonitoredThreadFactory;
import cn.t.ytten.metricexposer.common.thread.MonitoredThreadPool;
import cn.t.ytten.metricexposer.common.thread.ThreadPoolMonitor;

import java.util.concurrent.*;

public class ThreadUtil {

    private static final ScheduledExecutorService scheduledExecutorService =  Executors.newScheduledThreadPool(1);

    public static Future<?> submitTask(Runnable runnable) {
        return Socks5ThreadPoolHolder.THREAD_POOL_EXECUTOR.submit(runnable);
    }

    public static ScheduledFuture<?> scheduleTask(Runnable runnable, int initialDelayInSeconds, int periodInSeconds) {
        return scheduledExecutorService.scheduleWithFixedDelay(runnable, initialDelayInSeconds, periodInSeconds, TimeUnit.SECONDS);
    }

    public static void closeThreadPool() {
        if(!Socks5ThreadPoolHolder.THREAD_POOL_EXECUTOR.isShutdown()) {
            Socks5ThreadPoolHolder.THREAD_POOL_EXECUTOR.shutdownNow();
        }
        if(!scheduledExecutorService.isShutdown()) {
            scheduledExecutorService.shutdownNow();
        }
    }

    private static class Socks5ThreadPoolHolder {
        private static final ThreadPoolExecutor THREAD_POOL_EXECUTOR = new MonitoredThreadPool(
            ThreadPoolConfig.CORE_THREAD_COUNT,
            ThreadPoolConfig.MAX_THREAD_COUNT,
            ThreadPoolConfig.THREAD_TT,
            ThreadPoolConfig.THREAD_TT_TIME_UNIT,
            new ArrayBlockingQueue<>(ThreadPoolConfig.BLOCKING_THREAD_COUNT),
            new MonitoredThreadFactory(ThreadPoolConfig.THREAD_POOL_NAME),
            ThreadPoolConfig.THREAD_POOL_NAME
        );
        static {
            scheduleTask(new ThreadPoolMonitor(ThreadPoolConfig.THREAD_POOL_NAME, THREAD_POOL_EXECUTOR), 3, 5);
        }
    }
}

package cn.t.ytten.metricexposer.common.thread;

import cn.t.ytten.core.util.LoggingUtil;
import cn.t.ytten.metricexposer.common.constants.ThreadConstants;

import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * 线程池监控器
 *
 * @author <a href="mailto:jian.yang@liby.ltd">野生程序员-杨建</a>
 * @version V1.0
 * @since 2020-02-24 23:15
 **/
public class ThreadPoolMonitor implements Runnable {

    private static final Logger logger = LoggingUtil.getLogger(ThreadConstants.THREAD_POOL_MONITOR_LOG_NAME);

    private final String poolName;
    private final ThreadPoolExecutor threadPoolExecutor;

    @Override
    public void run() {
        logger.info(String.format("%s-monitor: " +
                        " 当前线程数量: %d, 核心线程数量: %d, 正在执行任务的线程数量: %d, " +
                        "已完成任务数量: %d, 任务总数: %d, 队列里缓存的任务数量: %d, 池中存在的最大线程数: %d, " +
                        "最大允许的线程数: %d,  线程空闲时间: %d, 线程池是否关闭: %b, 线程池是否终止: %b",
                this.poolName,
                threadPoolExecutor.getPoolSize(), threadPoolExecutor.getCorePoolSize(), threadPoolExecutor.getActiveCount(),
                threadPoolExecutor.getCompletedTaskCount(), threadPoolExecutor.getTaskCount(), threadPoolExecutor.getQueue().size(), threadPoolExecutor.getLargestPoolSize(),
                threadPoolExecutor.getMaximumPoolSize(), threadPoolExecutor.getKeepAliveTime(TimeUnit.MILLISECONDS), threadPoolExecutor.isShutdown(), threadPoolExecutor.isTerminated()));
    }

    public ThreadPoolMonitor(String poolName, ThreadPoolExecutor threadPoolExecutor) {
        this.poolName = poolName;
        this.threadPoolExecutor = threadPoolExecutor;
    }
}

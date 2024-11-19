package cn.t.ytten.metricexposer.common.constants;

import java.util.concurrent.TimeUnit;

public class ThreadPoolConfig {
    public static final String THREAD_POOL_NAME = "async-task";
    public static final int PROCESSOR_COUNT = Runtime.getRuntime().availableProcessors();
    public static final int CORE_THREAD_COUNT = (PROCESSOR_COUNT < 4 ? 2 : PROCESSOR_COUNT) * 4;
    public static final int BLOCKING_THREAD_COUNT = 3;
    public static final int MAX_THREAD_COUNT = (CORE_THREAD_COUNT + BLOCKING_THREAD_COUNT) * 2;
    public static final int THREAD_TT = 10;
    public static final TimeUnit THREAD_TT_TIME_UNIT = TimeUnit.SECONDS;
}

package cn.t.ytten.metricexposer.client.collector;


import cn.t.ytten.metricexposer.common.message.metrics.batch.BatchNetworkMetric;

import java.util.concurrent.TimeUnit;

public interface NetWorkMetricCollector {
    long ONE_SECOND = TimeUnit.SECONDS.toNanos(1);
    BatchNetworkMetric bytePerSecond();
    boolean test();
}

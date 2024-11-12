package cn.t.ytten.metricexposer.client.collector.impl;

import cn.t.ytten.metricexposer.client.collector.NetWorkMetricCollector;
import cn.t.ytten.metricexposer.common.message.metrics.batch.BatchNetworkMetric;

import java.util.Collections;

public class DefaultNetworkMetricCollector implements NetWorkMetricCollector {

    private final BatchNetworkMetric batchNetworkMetric;

    {
        batchNetworkMetric = new BatchNetworkMetric();
        batchNetworkMetric.setNetworkMetricList(Collections.emptyList());
    }
    @Override
    public BatchNetworkMetric bytePerSecond() {
        return batchNetworkMetric;
    }

    @Override
    public boolean test() {
        return true;
    }
}

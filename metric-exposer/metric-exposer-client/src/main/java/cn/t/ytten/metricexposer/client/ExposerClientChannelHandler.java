package cn.t.ytten.metricexposer.client;

import cn.t.ytten.core.channel.ChannelContext;
import cn.t.ytten.core.channel.ChannelHandler;
import cn.t.ytten.metricexposer.common.message.infos.SystemInfo;
import cn.t.ytten.metricexposer.common.message.metrics.CpuLoadMetric;
import cn.t.ytten.metricexposer.common.message.metrics.MemoryMetric;
import cn.t.ytten.metricexposer.common.message.metrics.batch.BatchDiscMetric;
import cn.t.ytten.metricexposer.common.message.metrics.batch.BatchNetworkMetric;
import cn.t.ytten.metricexposer.common.util.ThreadUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

public class ExposerClientChannelHandler implements ChannelHandler {

    private static final int delayTime = 3;
    private final List<ScheduledFuture<?>> scheduledFutureListList = new ArrayList<>(4);

    @Override
    public void ready(ChannelContext ctx) {
        //系统信息
        ThreadUtil.submitTask(() -> {
            SystemInfo message = MetricCollectUtil.collectSystemInfo();
            ctx.getEventLoop().addTask(() -> {
                ctx.invokeChannelWrite(message);
                ctx.flush();
            });
        });
        //cpu采集
        scheduledFutureListList.add(ThreadUtil.scheduleTask(() -> {
            CpuLoadMetric message = MetricCollectUtil.collectCpuMetric();
            ctx.getEventLoop().addTask(() -> {
                ctx.invokeChannelWrite(message);
                ctx.flush();
            });
        }, 0, delayTime));
        //内存采集
        scheduledFutureListList.add(ThreadUtil.scheduleTask(() -> {
            MemoryMetric message = MetricCollectUtil.collectMemoryMetric();
            ctx.getEventLoop().addTask(() -> {
                ctx.invokeChannelWrite(message);
                ctx.flush();
            });
        }, 0, delayTime));
        //network采集
        scheduledFutureListList.add(ThreadUtil.scheduleTask(() -> {
            BatchNetworkMetric message = MetricCollectUtil.collectBatchNetworkMetric();
            ctx.getEventLoop().addTask(() -> {
                ctx.invokeChannelWrite(message);
                ctx.flush();
            });
        }, 0, delayTime));
        //磁盘采集
        scheduledFutureListList.add(ThreadUtil.scheduleTask(() -> {
            BatchDiscMetric message = MetricCollectUtil.collectBatchDiscMetric();
            ctx.getEventLoop().addTask(() -> {
                ctx.invokeChannelWrite(message);
                ctx.flush();
            });
        }, 0, delayTime));
    }

    @Override
    public void close(ChannelContext ctx) throws Exception {
        scheduledFutureListList.forEach(scheduledFuture -> scheduledFuture.cancel(true));
    }
}

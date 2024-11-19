package cn.t.ytten.metricexposer.client;

import cn.t.ytten.core.channel.ChannelContext;
import cn.t.ytten.core.channel.ChannelHandler;
import cn.t.ytten.core.eventloop.EventLoopDelayTask;
import cn.t.ytten.metricexposer.common.message.infos.SystemInfo;
import cn.t.ytten.metricexposer.common.message.metrics.CpuLoadMetric;
import cn.t.ytten.metricexposer.common.message.metrics.MemoryMetric;
import cn.t.ytten.metricexposer.common.message.metrics.batch.BatchDiscMetric;
import cn.t.ytten.metricexposer.common.message.metrics.batch.BatchNetworkMetric;

public class ExposerClientChannelHandler implements ChannelHandler {
    private static final long delayTime = 3000;
    @Override
    public void ready(ChannelContext ctx) throws Exception {
        //系统消息
        ctx.getEventLoop().addTask(() -> {
            SystemInfo message = MetricCollectUtil.collectSystemInfo();
            ctx.invokeChannelWrite(message);
            ctx.flush();
        });
        //cpu采集
        ctx.getEventLoop().addDelayTask(new EventLoopDelayTask(delayTime, true, () -> {
            CpuLoadMetric message = MetricCollectUtil.collectCpuMetric();
            ctx.invokeChannelWrite(message);
            ctx.flush();
        }));
        //内存采集
        ctx.getEventLoop().addDelayTask(new EventLoopDelayTask(delayTime, true, () -> {
            MemoryMetric message = MetricCollectUtil.collectMemoryMetric();
            ctx.invokeChannelWrite(message);
            ctx.flush();
        }));
        //network采集
        ctx.getEventLoop().addDelayTask(new EventLoopDelayTask(delayTime, true, () -> {
            BatchNetworkMetric message = MetricCollectUtil.collectBatchMetric();
            ctx.invokeChannelWrite(message);
            ctx.flush();
        }));
        //磁盘采集
        ctx.getEventLoop().addDelayTask(new EventLoopDelayTask(delayTime, true, () -> {
            BatchDiscMetric message = MetricCollectUtil.collectBatchDiscMetric();
            ctx.invokeChannelWrite(message);
            ctx.flush();
        }));
    }
}

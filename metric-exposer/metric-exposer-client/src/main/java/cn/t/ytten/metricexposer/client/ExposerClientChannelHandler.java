package cn.t.ytten.metricexposer.client;

import cn.t.ytten.core.channel.ChannelContext;
import cn.t.ytten.core.channel.ChannelHandler;
import cn.t.ytten.metricexposer.common.message.infos.SystemInfo;

public class ExposerClientChannelHandler implements ChannelHandler {
    @Override
    public void ready(ChannelContext ctx) throws Exception {
        ctx.getEventLoop().addTask(() -> {
            SystemInfo systemInfo = MetricCollectUtil.collectSystemInfo();
            ctx.invokeChannelWrite(systemInfo);
            ctx.flush();
        });
    }
}

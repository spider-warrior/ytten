package cn.t.ytten.metricexposer.server;

import cn.t.ytten.core.channel.ChannelContext;
import cn.t.ytten.core.channel.ChannelInitializer;
import cn.t.ytten.metricexposer.common.handler.MessageCodec;

import java.nio.channels.SelectableChannel;

public class ExposerServerChannelInitializer implements ChannelInitializer {
    @Override
    public void initChannel(ChannelContext ctx, SelectableChannel channel) {
        ctx.getPipeline().addChannelHandlerLast(new MessageCodec());
        ctx.getPipeline().addChannelHandlerLast(new LogMessageChannelHandler());
    }
}

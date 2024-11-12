package cn.t.ytten.metricexposer.server;

import cn.t.ytten.core.channel.ChannelContext;
import cn.t.ytten.core.channel.ChannelInitializer;
import cn.t.ytten.core.channel.handler.ServerDebugChannelHandler;
import cn.t.ytten.metricexposer.common.handler.MessageCodec;

import java.nio.channels.Channel;

public class ExposerServerChannelInitializer implements ChannelInitializer {
    @Override
    public void initChannel(ChannelContext ctx, Channel channel) {
        ctx.getPipeline().addChannelHandlerLast(new MessageCodec());
        ctx.getPipeline().addChannelHandlerLast(new ServerDebugChannelHandler());
    }
}

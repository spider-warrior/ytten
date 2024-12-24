package cn.t.ytten.core.channel.initializer;

import cn.t.ytten.core.channel.ChannelContext;
import cn.t.ytten.core.channel.ChannelInitializer;
import cn.t.ytten.core.channel.handler.ServerDebugChannelHandler;

import java.nio.channels.SelectableChannel;

public class ServerDebugSocketChannelInitializer implements ChannelInitializer {
    @Override
    public void initChannel(ChannelContext ctx, SelectableChannel channel) {
        ctx.getPipeline().addChannelHandlerLast(new ServerDebugChannelHandler());
    }
}

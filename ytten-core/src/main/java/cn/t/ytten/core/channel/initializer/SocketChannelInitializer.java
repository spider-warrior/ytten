package cn.t.ytten.core.channel.initializer;

import cn.t.ytten.core.channel.ChannelContext;
import cn.t.ytten.core.channel.ChannelInitializer;
import cn.t.ytten.core.channel.handler.ServerDebugChannelHandler;

import java.nio.channels.Channel;

public class SocketChannelInitializer implements ChannelInitializer {
    @Override
    public void initChannel(ChannelContext ctx, Channel channel) throws Exception {
        ctx.getPipeline().addChannelHandlerLast(new ServerDebugChannelHandler());
    }
}

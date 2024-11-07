package cn.t.ytten.core.channel.initializer;

import cn.t.ytten.core.channel.ChannelContext;
import cn.t.ytten.core.channel.Channelnitializer;
import cn.t.ytten.core.channel.handler.DebugChannelHandler;

import java.nio.channels.SocketChannel;

public class SocketChannelInitializer implements Channelnitializer<SocketChannel> {
    @Override
    public void initChannel(ChannelContext ctx, SocketChannel socketChannel) throws Exception {
        ctx.getPipeline().addChannelHandlerLast(new DebugChannelHandler());
    }
}

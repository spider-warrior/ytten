package cn.t.ytten.core.channel.handler;

import cn.t.ytten.core.channel.ChannelContext;
import cn.t.ytten.core.channel.ChannelHandler;
import cn.t.ytten.core.channel.initializer.ServerChannelInitializer;

import java.net.StandardSocketOptions;
import java.nio.channels.SocketChannel;

public class ConnectionAcceptHandler implements ChannelHandler {

    private ServerChannelInitializer initializer;

    @Override
    public void read(ChannelContext ctx, Object msg) throws Exception {
        SocketChannel socketChannel = (SocketChannel)msg;
        socketChannel.configureBlocking(false);
        socketChannel.setOption(StandardSocketOptions.SO_KEEPALIVE, false);
        socketChannel.setOption(StandardSocketOptions.TCP_NODELAY, false);
        //注册读事件
        ctx.getEventLoop().addTask(()-> null);
    }

    public ConnectionAcceptHandler(ServerChannelInitializer initializer) {
        this.initializer = initializer;
    }
}

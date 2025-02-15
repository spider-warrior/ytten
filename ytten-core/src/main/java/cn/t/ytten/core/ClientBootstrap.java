package cn.t.ytten.core;

import cn.t.ytten.core.channel.ChannelContext;
import cn.t.ytten.core.channel.ChannelInitializer;
import cn.t.ytten.core.eventloop.SingleThreadEventLoop;
import cn.t.ytten.core.exception.ChannelException;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.StandardSocketOptions;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public class ClientBootstrap {
    public void start(String host, int port, SingleThreadEventLoop ioEventLoop, ChannelInitializer initializer) {
        SocketAddress remoteAddress = new InetSocketAddress(host, port);
        ioEventLoop.addTask(() -> {
            try {
                //连接
                SocketChannel socketChannel = connect(host, port);
                //初始化context
                ChannelContext ctx = initChannelContext(socketChannel, remoteAddress, ioEventLoop, initializer);
                //监听connect事件
                ctx.register(ioEventLoop.getSelector(), SelectionKey.OP_CONNECT, ctx);
            } catch (IOException e) {
                throw new ChannelException(e);
            }
        });
        Thread ioThread = new Thread(ioEventLoop, ioEventLoop.getName());
        ioThread.start();
    }

    private ChannelContext initChannelContext(SocketChannel socketChannel, SocketAddress remoteAddress, SingleThreadEventLoop ioEventLoop, ChannelInitializer initializer) {
        ChannelContext ctx = ChannelContext.socketChannelContext(socketChannel, remoteAddress, ioEventLoop);
        initializer.initChannel(ctx, socketChannel);
        return ctx;
    }

    private SocketChannel connect(String host, int port) throws IOException {
        SocketChannel socketChannel = SocketChannel.open();
        socketChannel.setOption(StandardSocketOptions.SO_KEEPALIVE, false);
        socketChannel.setOption(StandardSocketOptions.TCP_NODELAY, false);
        socketChannel.configureBlocking(false);
        socketChannel.connect(new InetSocketAddress(host, port));
        return socketChannel;
    }
}

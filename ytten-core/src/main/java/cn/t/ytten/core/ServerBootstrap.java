package cn.t.ytten.core;

import cn.t.ytten.core.channel.ChannelContext;
import cn.t.ytten.core.channel.handler.ConnectionAcceptHandler;
import cn.t.ytten.core.channel.initializer.ServerChannelInitializer;
import cn.t.ytten.core.eventloop.SingleThreadEventLoop;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;

public class ServerBootstrap {

    private final SingleThreadEventLoop acceptEventLoop;
    private final SingleThreadEventLoop ioHandleEventLoop;

    private ServerSocketChannel bind(int port) throws IOException {
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.setOption(StandardSocketOptions.SO_REUSEADDR, true);
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.socket().bind(new InetSocketAddress(port), 128);
        return serverSocketChannel;
    }

    private ChannelContext initChannelContext(ServerSocketChannel serverSocketChannel, SingleThreadEventLoop ioHandleEventLoop) {
        ChannelContext ctx = ChannelContext.serverSocketChannelContext(serverSocketChannel, acceptEventLoop);
        ctx.getPipeline().addChannelHandlerLast(new ConnectionAcceptHandler(new ServerChannelInitializer(), ioHandleEventLoop));
        return ctx;
    }

    public void start(int port) {
        acceptEventLoop.addTask(() -> {
            //构建ServerSocketChannel
            return bind(port);
        }).map(channel -> {
            //初始化context
            return initChannelContext(channel, ioHandleEventLoop);
        }).map(ctx -> {
            // 监听事件
            ctx.register(acceptEventLoop.getSelector(), SelectionKey.OP_ACCEPT);
            return ctx;
        });
        Thread acceptThread = new Thread(acceptEventLoop);
        acceptThread.start();
        Thread ioThread = new Thread(ioHandleEventLoop);
        ioThread.start();
    }

    public ServerBootstrap(SingleThreadEventLoop acceptEventLoop, SingleThreadEventLoop ioHandleEventLoop) {
        this.acceptEventLoop = acceptEventLoop;
        this.ioHandleEventLoop = ioHandleEventLoop;
    }
}

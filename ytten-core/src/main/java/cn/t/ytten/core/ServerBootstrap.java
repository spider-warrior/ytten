package cn.t.ytten.core;

import cn.t.ytten.core.channel.ChannelContext;
import cn.t.ytten.core.channel.handler.ConnectionAcceptHandler;
import cn.t.ytten.core.channel.initializer.ServerChannelInitializer;
import cn.t.ytten.core.eventloop.ExecuteChain;
import cn.t.ytten.core.eventloop.SingleThreadEventLoop;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;

public class ServerBootstrap {

    private final SingleThreadEventLoop acceptEventLoop;
    private final SingleThreadEventLoop ioEventLoop;

    private ServerSocketChannel bind(int port) throws IOException {
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.setOption(StandardSocketOptions.SO_REUSEADDR, true);
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.socket().bind(new InetSocketAddress(port), 128);
        return serverSocketChannel;
    }

    private ChannelContext initChannelContext(ServerSocketChannel serverSocketChannel, SingleThreadEventLoop ioEventLoop) {
        ChannelContext ctx = ChannelContext.serverSocketChannelContext(serverSocketChannel, acceptEventLoop);
        ctx.getPipeline().addChannelHandlerLast(new ConnectionAcceptHandler(new ServerChannelInitializer(), ioEventLoop));
        return ctx;
    }

    public void start(int port) {
        acceptEventLoop.addTask(new ExecuteChain<>(() -> {
            //构建ServerSocketChannel
            return bind(port);
        }).map(channel -> {
            System.out.println("端口绑定成功: " + channel.socket());
            //初始化context
            return initChannelContext(channel, ioEventLoop);
        }).map(ctx -> {
            System.out.println("serverChannel初始化完毕");
            // 监听事件
            ctx.register(acceptEventLoop.getSelector(), SelectionKey.OP_ACCEPT).attach(ctx);
            return ctx;
        }).map(ctx -> {
            System.out.println("accept事件注册成功");
            return ctx;
        }));
        Thread acceptThread = new Thread(acceptEventLoop);
        acceptThread.start();
        if(acceptEventLoop != ioEventLoop) {
            Thread ioThread = new Thread(ioEventLoop);
            ioThread.start();
        }
    }

    public ServerBootstrap(SingleThreadEventLoop acceptEventLoop, SingleThreadEventLoop ioEventLoop) {
        this.acceptEventLoop = acceptEventLoop;
        this.ioEventLoop = ioEventLoop;
    }
}

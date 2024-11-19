package cn.t.ytten.core;

import cn.t.ytten.core.channel.ChannelContext;
import cn.t.ytten.core.channel.ChannelInitializer;
import cn.t.ytten.core.channel.handler.ConnectionAcceptHandler;
import cn.t.ytten.core.eventloop.SingleThreadEventLoop;
import cn.t.ytten.core.exception.ChannelException;
import cn.t.ytten.core.util.LoggingUtil;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.util.logging.Logger;

public class ServerBootstrap {

    private static final Logger logger = LoggingUtil.getLogger(ServerBootstrap.class);

    private ServerSocketChannel bind(int port) throws IOException {
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.setOption(StandardSocketOptions.SO_REUSEADDR, true);
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.socket().bind(new InetSocketAddress(port), 128);
        return serverSocketChannel;
    }

    private ChannelContext initChannelContext(ServerSocketChannel serverSocketChannel, SingleThreadEventLoop acceptEventLoop, SingleThreadEventLoop ioEventLoop, ChannelInitializer initializer) {
        ChannelContext ctx = ChannelContext.serverSocketChannelContext(serverSocketChannel, acceptEventLoop);
        ctx.getPipeline().addChannelHandlerLast(new ConnectionAcceptHandler(initializer, ioEventLoop, acceptEventLoop == ioEventLoop));
        return ctx;
    }

    public void start(int port, SingleThreadEventLoop acceptEventLoop, SingleThreadEventLoop ioEventLoop, ChannelInitializer initializer) {
        acceptEventLoop.addTask(() -> {
            try {
                //构建ServerSocketChannel
                ServerSocketChannel serverSocketChannel = bind(port);
                logger.info("端口绑定完成: " + serverSocketChannel.socket());
                ChannelContext ctx = initChannelContext(serverSocketChannel, acceptEventLoop, ioEventLoop, initializer);
                logger.info("serverChannel初始化完成");
                //监听accept事件
                ctx.register(acceptEventLoop.getSelector(), SelectionKey.OP_ACCEPT).attach(ctx);
                logger.info("accept注册完成");
            } catch (IOException e) {
                throw new ChannelException(e);
            }
        });
        Thread acceptThread = new Thread(acceptEventLoop, acceptEventLoop.getName());
        acceptThread.start();
        if(acceptEventLoop != ioEventLoop) {
            Thread ioThread = new Thread(ioEventLoop, ioEventLoop.getName());
            ioThread.start();
        }
    }

}

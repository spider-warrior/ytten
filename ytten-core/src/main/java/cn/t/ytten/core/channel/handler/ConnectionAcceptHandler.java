package cn.t.ytten.core.channel.handler;

import cn.t.ytten.core.ServerBootstrap;
import cn.t.ytten.core.channel.ChannelContext;
import cn.t.ytten.core.channel.ChannelHandler;
import cn.t.ytten.core.channel.ChannelInitializer;
import cn.t.ytten.core.eventloop.SingleThreadEventLoop;
import cn.t.ytten.core.util.LoggingUtil;

import java.net.StandardSocketOptions;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.logging.Logger;

public class ConnectionAcceptHandler implements ChannelHandler {

    private static final Logger logger = LoggingUtil.getLogger(ServerBootstrap.class);

    private final ChannelInitializer initializer;
    private final SingleThreadEventLoop ioEventLoop;
    private final boolean syncRegister;

    @Override
    public void read(ChannelContext ctx, Object msg) throws Exception {
        SocketChannel socketChannel = (SocketChannel)msg;
        logger.info("接收新客户端连接: " + socketChannel.socket().getRemoteSocketAddress());
        socketChannel.configureBlocking(false);
        socketChannel.setOption(StandardSocketOptions.SO_KEEPALIVE, false);
        socketChannel.setOption(StandardSocketOptions.TCP_NODELAY, false);
        //构建subContext
        ChannelContext subCtx = ChannelContext.socketChannelContext(socketChannel, socketChannel.getRemoteAddress(), ioEventLoop);
        initializer.initChannel(subCtx, socketChannel);
        //连接就绪
        subCtx.getPipeline().invokeChannelReady(subCtx);
        //注册读事件
        if(syncRegister) {
            subCtx.register(ioEventLoop.getSelector(), SelectionKey.OP_READ).attach(subCtx);
        } else {
            //有可能会等到下次loop才能执行
            ioEventLoop.addTask(() -> subCtx.register(ioEventLoop.getSelector(), SelectionKey.OP_READ).attach(subCtx));
        }
    }

    public ConnectionAcceptHandler(ChannelInitializer initializer, SingleThreadEventLoop ioEventLoop, boolean syncRegister) {
        this.initializer = initializer;
        this.ioEventLoop = ioEventLoop;
        this.syncRegister = syncRegister;
    }
}

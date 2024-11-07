package cn.t.ytten.core.channel.handler;

import cn.t.ytten.core.channel.ChannelContext;
import cn.t.ytten.core.channel.ChannelHandler;
import cn.t.ytten.core.channel.initializer.SocketChannelInitializer;
import cn.t.ytten.core.eventloop.ExecuteChain;
import cn.t.ytten.core.eventloop.SingleThreadEventLoop;

import java.net.StandardSocketOptions;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public class ConnectionAcceptHandler implements ChannelHandler {

    private final SocketChannelInitializer initializer;
    private final SingleThreadEventLoop ioEventLoop;
    private final boolean syncRegister;

    @Override
    public void read(ChannelContext ctx, Object msg) throws Exception {
        SocketChannel socketChannel = (SocketChannel)msg;
        socketChannel.configureBlocking(false);
        socketChannel.setOption(StandardSocketOptions.SO_KEEPALIVE, false);
        socketChannel.setOption(StandardSocketOptions.TCP_NODELAY, false);
        //构建subContext
        ChannelContext subCtx = ChannelContext.socketChannelContext(socketChannel, ioEventLoop);
        initializer.initChannel(subCtx, socketChannel);
        //注册读事件
        if(syncRegister) {
            subCtx.register(ioEventLoop.getSelector(), SelectionKey.OP_READ).attach(subCtx);
        } else {
            //有可能会等到下次loop才能执行
            ioEventLoop.addTask(new ExecuteChain<>(() -> {
                subCtx.register(ioEventLoop.getSelector(), SelectionKey.OP_READ).attach(subCtx);
                return subCtx;
            }));
        }
    }

    public ConnectionAcceptHandler(SocketChannelInitializer initializer, SingleThreadEventLoop ioEventLoop, boolean syncRegister) {
        this.initializer = initializer;
        this.ioEventLoop = ioEventLoop;
        this.syncRegister = syncRegister;
    }
}

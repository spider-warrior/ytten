package cn.t.ytten.core.channel.handler;

import cn.t.ytten.core.channel.ChannelContext;
import cn.t.ytten.core.channel.ChannelHandler;
import cn.t.ytten.core.channel.UnPooledHeapByteBuf;
import cn.t.ytten.core.channel.initializer.ServerChannelInitializer;
import cn.t.ytten.core.eventloop.ExecuteChain;
import cn.t.ytten.core.eventloop.SingleThreadEventLoop;

import java.net.StandardSocketOptions;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public class ConnectionAcceptHandler implements ChannelHandler {

    private final ServerChannelInitializer initializer;
    private final SingleThreadEventLoop ioEventLoop;

    @Override
    public void read(ChannelContext ctx, Object msg) throws Exception {
        SocketChannel socketChannel = (SocketChannel)msg;
        socketChannel.configureBlocking(false);
        socketChannel.setOption(StandardSocketOptions.SO_KEEPALIVE, false);
        socketChannel.setOption(StandardSocketOptions.TCP_NODELAY, false);
        //构建subContext
        ChannelContext subCtx = ChannelContext.socketChannelContext(socketChannel, ioEventLoop);
        subCtx.getPipeline().addChannelHandlerLast(new ChannelHandler() {
            @Override
            public void ready(ChannelContext ctx) throws Exception {
                System.out.println("channel ready: " + ctx.getSelectableChannel());
            }

            @Override
            public void read(ChannelContext ctx, Object msg) throws Exception {
                UnPooledHeapByteBuf byteBuf = (UnPooledHeapByteBuf)msg;
                byte[] content = new byte[byteBuf.readableBytes()];
                byteBuf.readBytes(content);
                System.out.println("channel read: " + new String(content));
            }

            @Override
            public void write(ChannelContext ctx, Object msg) throws Exception {
                System.out.println("channel write: " + ctx.getSelectableChannel());
            }

            @Override
            public void close(ChannelContext ctx) throws Exception {
                System.out.println("channel close: " + ctx.getSelectableChannel());
            }

            @Override
            public void error(ChannelContext ctx, Throwable t) throws Exception {
                System.out.println("channel error: " + ctx.getSelectableChannel());
            }
        });
        //注册读事件
        ioEventLoop.addTask(new ExecuteChain<>(() -> {
            subCtx.register(ioEventLoop.getSelector(), SelectionKey.OP_READ).attach(subCtx);
            return subCtx;
        }));
    }

    public ConnectionAcceptHandler(ServerChannelInitializer initializer, SingleThreadEventLoop ioEventLoop) {
        this.initializer = initializer;
        this.ioEventLoop = ioEventLoop;
    }
}

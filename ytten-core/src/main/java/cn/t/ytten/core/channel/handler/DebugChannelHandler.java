package cn.t.ytten.core.channel.handler;

import cn.t.ytten.core.channel.ChannelContext;
import cn.t.ytten.core.channel.ChannelHandler;
import cn.t.ytten.core.channel.UnPooledHeapByteBuf;

public class DebugChannelHandler implements ChannelHandler {
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
}

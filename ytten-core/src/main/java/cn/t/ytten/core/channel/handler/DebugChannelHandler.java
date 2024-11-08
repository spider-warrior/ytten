package cn.t.ytten.core.channel.handler;

import cn.t.ytten.core.channel.ChannelContext;
import cn.t.ytten.core.channel.ChannelHandler;
import cn.t.ytten.core.channel.UnPooledHeapByteBuf;
import cn.t.ytten.core.util.LoggingUtil;

import java.util.logging.Logger;

public class DebugChannelHandler implements ChannelHandler {

    private static final Logger logger = LoggingUtil.getLogger(DebugChannelHandler.class);

    @Override
    public void ready(ChannelContext ctx) throws Exception {
        logger.info("channel ready: " + ctx.getSelectableChannel());
    }

    @Override
    public void read(ChannelContext ctx, Object msg) throws Exception {
        UnPooledHeapByteBuf byteBuf = (UnPooledHeapByteBuf)msg;
        byte[] content = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(content);
        logger.info("channel read: " + new String(content));
    }

    @Override
    public void write(ChannelContext ctx, Object msg) throws Exception {
        logger.info("channel write: " + ctx.getSelectableChannel());
    }

    @Override
    public void close(ChannelContext ctx) throws Exception {
        logger.info("channel close: " + ctx.getSelectableChannel());
    }

    @Override
    public void error(ChannelContext ctx, Throwable t) throws Exception {
        logger.info("channel error: " + ctx.getSelectableChannel());
    }
}

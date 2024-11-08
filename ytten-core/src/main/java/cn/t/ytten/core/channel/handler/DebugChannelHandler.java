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
        ctx.getWriteCache().writeBytes("hello".getBytes());
        ctx.flush();
    }

    @Override
    public void read(ChannelContext ctx, Object msg) throws Exception {
//        if(true) {
//            throw new RuntimeException("on purpose");
//        }
        UnPooledHeapByteBuf byteBuf = (UnPooledHeapByteBuf)msg;
        byte[] content = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(content);
        logger.info("channel read: " + new String(content));
        ctx.invokeChannelWrite(String.valueOf(System.currentTimeMillis()));
        ctx.flush();
    }

    @Override
    public void write(ChannelContext ctx, Object msg) throws Exception {
        logger.info("channel write: " + msg);
        if(msg != null) {
            ctx.getWriteCache().writeBytes(msg.toString().getBytes());
        }
    }

    @Override
    public void close(ChannelContext ctx) throws Exception {
        logger.info("channel close: " + ctx.getSelectableChannel());
    }

//    @Override
//    public void error(ChannelContext ctx, Throwable t) throws Exception {
//        logger.info("channel error: " + ctx.getSelectableChannel());
//    }
}

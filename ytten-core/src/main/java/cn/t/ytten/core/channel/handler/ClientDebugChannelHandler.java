package cn.t.ytten.core.channel.handler;

import cn.t.ytten.core.channel.ChannelContext;
import cn.t.ytten.core.channel.ChannelHandler;
import cn.t.ytten.core.util.LoggingUtil;

import java.util.logging.Logger;

public class ClientDebugChannelHandler implements ChannelHandler {

    private static final Logger logger = LoggingUtil.getLogger(ClientDebugChannelHandler.class);

    @Override
    public void ready(ChannelContext ctx) throws Exception {
        logger.info("client connect channel ready: " + ctx.remoteAddress());
        ctx.getWriteCache().writeBytes("client hello".getBytes());
        ctx.flush();
    }

    @Override
    public void read(ChannelContext ctx, Object msg) throws Exception {
        logger.info("client connect channel read: " + msg);
    }

    @Override
    public void write(ChannelContext ctx, Object msg) throws Exception {
        logger.info("client connect channel write: " + msg);
        if(msg != null) {
            ctx.getWriteCache().writeBytes(msg.toString().getBytes());
        }
    }

    @Override
    public void close(ChannelContext ctx) throws Exception {
        logger.info("client connect channel close: " + ctx.remoteAddress());
    }

    @Override
    public void error(ChannelContext ctx, Throwable t) throws Exception {
        logger.info("client connect channel error: " + ctx.remoteAddress());
        ctx.close();
    }
}

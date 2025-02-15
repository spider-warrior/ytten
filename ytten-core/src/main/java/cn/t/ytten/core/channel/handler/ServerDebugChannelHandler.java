package cn.t.ytten.core.channel.handler;

import cn.t.ytten.core.channel.ChannelContext;
import cn.t.ytten.core.channel.ChannelHandler;
import cn.t.ytten.core.util.LoggingUtil;

import java.time.LocalDateTime;
import java.util.logging.Logger;

public class ServerDebugChannelHandler implements ChannelHandler {

    private static final Logger logger = LoggingUtil.getLogger(ServerDebugChannelHandler.class);

    @Override
    public void ready(ChannelContext ctx) throws Exception {
        logger.info("server accept channel ready: " + ctx.remoteAddress());
        ctx.getWriteCache().writeBytes("server hello".getBytes());
        ctx.flush();
    }

    @Override
    public void read(ChannelContext ctx, Object msg) throws Exception {
//        if(System.currentTimeMillis() % 3 == 0) {
//            ctx.close();
//            throw new RuntimeException("on purpose");
//        }
        logger.info("server accept channel read: " + msg);
        ctx.getPipeline().invokeChannelWrite(ctx, "服务端当前时间: " + LocalDateTime.now());
        ctx.flush();

    }

    @Override
    public void write(ChannelContext ctx, Object msg) throws Exception {
        logger.info("server accept channel write: " + msg);
        if(msg != null) {
            ctx.getWriteCache().writeBytes(msg.toString().getBytes());
        }
    }

    @Override
    public void close(ChannelContext ctx) throws Exception {
        logger.info("server accept channel close: " + ctx.remoteAddress());

    }

    @Override
    public void error(ChannelContext ctx, Throwable t) throws Exception {
        logger.info("server accept channel error: " + ctx.remoteAddress());
        ctx.close();
    }
}

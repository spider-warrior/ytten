package cn.t.ytten.core.channel;


public interface ChannelHandler {
    default void ready(ChannelContext ctx) throws Exception {
        ctx.getPipeline().invokeNextChannelReady(ctx.getPipeline().nextHandler(this), ctx);
    }
    default void read(ChannelContext ctx, Object msg) throws Exception {
        ctx.getPipeline().invokeNextChannelRead(ctx.getPipeline().nextHandler(this), ctx, msg);
    }
    default void write(ChannelContext ctx, Object msg) throws Exception {
        ctx.getPipeline().invokeNextChannelWrite(ctx.getPipeline().nextHandler(this), ctx, msg);
    }
    default void close(ChannelContext ctx) throws Exception {
        ctx.getPipeline().invokeNextChannelClose(ctx.getPipeline().nextHandler(this), ctx);
    }
    default void error(ChannelContext ctx, Throwable t) throws Exception {
        ctx.getPipeline().invokeNextChannelError(ctx.getPipeline().nextHandler(this), ctx, t);
    }
}

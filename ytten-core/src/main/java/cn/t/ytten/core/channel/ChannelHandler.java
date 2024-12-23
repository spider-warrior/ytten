package cn.t.ytten.core.channel;


public interface ChannelHandler {
    default void ready(ChannelContext ctx) throws Exception {
        ctx.invokeNextChannelReady(ctx.getPipeline().nextHandler(this));
    }
    default void read(ChannelContext ctx, Object msg) throws Exception {
        ctx.invokeNextChannelRead(ctx.getPipeline().nextHandler(this), msg);
    }
    default void write(ChannelContext ctx, Object msg) throws Exception {
        ctx.invokeNextChannelWrite(ctx.getPipeline().nextHandler(this), msg);
    }
    default void close(ChannelContext ctx) throws Exception {
        ctx.invokeNextChannelClose(ctx.getPipeline().nextHandler(this));
    }
    default void error(ChannelContext ctx, Throwable t) throws Exception {
        ctx.invokeNextChannelError(ctx.getPipeline().nextHandler(this), t);
    }
}

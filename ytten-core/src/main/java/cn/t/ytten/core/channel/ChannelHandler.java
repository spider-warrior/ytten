package cn.t.ytten.core.channel;


public interface ChannelHandler {
    default void ready(ChannelContext ctx) throws Exception {
        ctx.invokeNextChannelReady();
    }
    default void read(ChannelContext ctx, Object msg) throws Exception {
        ctx.invokeNextChannelRead(this, msg);
    }
    default void write(ChannelContext ctx, Object msg) throws Exception {
        ctx.invokeNextChannelWrite(this, msg);
    }
    default void close(ChannelContext ctx) throws Exception {
        ctx.invokeNextChannelClose(this);
    }
    default void error(ChannelContext ctx, Throwable t) throws Exception {
        ctx.invokeNextChannelError(this, t);
    }
}

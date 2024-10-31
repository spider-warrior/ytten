package cn.t.ytten.core.channel;


public interface ChannelHandler {
    default void ready(ChannelContext ctx) throws Exception {
        ctx.invokeNextChannelReady();
    }
    default void read(ChannelContext ctx, Object msg) throws Exception {
        ctx.invokeNextChannelRead(msg);
    }
    default void write(ChannelContext ctx, Object msg) throws Exception {
        ctx.invokeNextChannelWrite(msg);
    }
    default void close(ChannelContext ctx) throws Exception {
        ctx.invokeNextChannelClose();
    }
    default void error(ChannelContext ctx, Throwable t) throws Exception {
        ctx.invokeNextChannelError(t);
    }
}

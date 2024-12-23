package cn.t.ytten.core.channel;


import cn.t.ytten.core.exception.UnHandleException;
import cn.t.ytten.core.util.LoggingUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class ChannelPipeline {

    private static final Logger logger = LoggingUtil.getLogger(ChannelPipeline.class);
    private final List<ChannelHandler> channelHandlerList = new ArrayList<>();

    public void addChannelHandlerLast(ChannelHandler channelHandler) {
        this.channelHandlerList.add(channelHandler);
    }

    public void addChannelHandlerFirst(ChannelHandler channelHandler) {
        this.channelHandlerList.add(0, channelHandler);
    }

    public ChannelHandler nextHandler(ChannelHandler handler) {
        if(handler == null) {
            if(channelHandlerList.isEmpty()) {
                return null;
            } else {
                return channelHandlerList.get(0);
            }
        } else {
            for (int i = 0; i < channelHandlerList.size() - 1; i++) {
                if(channelHandlerList.get(i) == handler) {
                    int nextIndex = i + 1;
                    if(nextIndex < channelHandlerList.size()) {
                        return channelHandlerList.get(nextIndex);
                    }
                }
            }
            return null;
        }
    }

    public void invokeChannelReady(ChannelContext ctx) {
        if(channelHandlerList.isEmpty()) {
            logger.warning("channelReady事件未处理");
        } else {
            this.invokeNextChannelReady(nextHandler(null), ctx);
        }
    }

    public void invokeNextChannelReady(ChannelHandler handler, ChannelContext ctx) {
        try {
            handler.ready(ctx);
        } catch (Throwable t) {
            invokeChannelError(ctx, t);
        }
    }

    public void invokeChannelRead(ChannelContext ctx, Object msg) {
        if(channelHandlerList.isEmpty()) {
            logger.warning("channelRead事件未处理");
        } else {
            this.invokeNextChannelRead(nextHandler(null), ctx, msg);
        }
    }

    public void invokeNextChannelRead(ChannelHandler handler, ChannelContext ctx, Object msg) {
        try {
            handler.read(ctx, msg);
        } catch (Throwable t) {
            invokeChannelError(ctx, t);
        }
    }

    public void invokeChannelWrite(ChannelContext ctx, Object msg) {
        if(channelHandlerList.isEmpty()) {
            logger.warning("channelWrite事件未处理");
        } else {
            this.invokeNextChannelWrite(nextHandler(null), ctx, msg);
        }
    }

    public void invokeNextChannelWrite(ChannelHandler handler, ChannelContext ctx, Object msg) {
        try {
            handler.write(ctx, msg);
        } catch (Throwable t) {
            invokeChannelError(ctx, t);
        }
    }

    public void invokeChannelClose(ChannelContext ctx) {
        if(channelHandlerList.isEmpty()) {
            logger.warning("channelClose事件未处理");
        } else {
            this.invokeNextChannelClose(nextHandler(null), ctx);
        }
    }

    public void invokeNextChannelClose(ChannelHandler handler, ChannelContext ctx) {
        try {
            handler.close(ctx);
        } catch (Throwable t) {
            invokeChannelError(ctx, t);
        }
    }

    public void invokeChannelError(ChannelContext ctx, Throwable t) {
        if(channelHandlerList.isEmpty()) {
            logger.warning("channelError事件未处理");
        } else {
            this.invokeNextChannelError(nextHandler(null), ctx, t);
        }
    }

    public void invokeNextChannelError(ChannelHandler handler, ChannelContext ctx, Throwable t) {
        if(handler == null) {
            throw new UnHandleException(t);
        }
        try {
            handler.error(ctx, t);
        } catch (Throwable subThrowable) {
            invokeNextChannelError(nextHandler(handler), ctx, subThrowable);
        }
    }
}

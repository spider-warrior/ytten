package cn.t.ytten.core.channel;


import cn.t.ytten.core.exception.UnHandleException;
import cn.t.ytten.core.util.ExceptionUtil;
import cn.t.ytten.core.util.LoggingUtil;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

public class ChannelPipeline {

    private static final Logger defaultErrorHandlerLogger = LoggingUtil.getLogger("default-error-handler");

    private final List<ChannelHandler> channelHandlerList = new ArrayList<>();

    private Iterator<ChannelHandler> channelReadyIt;
    private Iterator<ChannelHandler> channelReadIt;
    private Iterator<ChannelHandler> channelWriteIt;
    private Iterator<ChannelHandler> channelCloseIt;
    private Iterator<ChannelHandler> channelErrorIt;

    public void addChannelHandlerLast(ChannelHandler channelHandler) {
        this.channelHandlerList.add(channelHandler);
    }

    public void addChannelHandlerFirst(ChannelHandler channelHandler) {
        this.channelHandlerList.add(0, channelHandler);
    }

    public void invokeChannelReady(ChannelContext ctx) {
        this.channelReadyIt = channelHandlerList.iterator();
        this.invokeNextChannelReady(ctx);
    }

    public void invokeNextChannelReady(ChannelContext ctx) {
        try {
            channelReadyIt.next().ready(ctx);
        } catch (Throwable t) {
            invokeChannelError(ctx, t);
        }
    }

    public void invokeChannelRead(ChannelContext ctx, Object msg) {
        this.channelReadIt = channelHandlerList.iterator();
        this.invokeNextChannelRead(ctx, msg);
    }

    public void invokeNextChannelRead(ChannelContext ctx, Object msg) {
        try {
            this.channelReadIt.next().read(ctx, msg);
        } catch (Throwable t) {
            invokeChannelError(ctx, t);
        }
    }

    public void invokeChannelWrite(ChannelContext ctx, Object msg) {
        this.channelWriteIt = channelHandlerList.iterator();
        this.invokeNextChannelWrite(ctx, msg);
    }

    public void invokeNextChannelWrite(ChannelContext ctx, Object msg) {
        try {
            this.channelWriteIt.next().write(ctx, msg);
        } catch (Throwable t) {
            invokeChannelError(ctx, t);
        }
    }

    public void invokeChannelClose(ChannelContext ctx) {
        this.channelCloseIt = channelHandlerList.iterator();
        this.invokeNextChannelClose(ctx);
    }

    public void invokeNextChannelClose(ChannelContext ctx) {
        try {
            this.channelCloseIt.next().close(ctx);
        } catch (Throwable t) {
            invokeChannelError(ctx, t);
        }
    }

    public void invokeChannelError(ChannelContext ctx, Throwable t) {
        this.channelErrorIt = channelHandlerList.iterator();
        this.invokeNextChannelError(ctx, t);
    }

    public void invokeNextChannelError(ChannelContext ctx, Throwable t) {
        try {
            if(channelErrorIt.hasNext()) {
                channelErrorIt.next().error(ctx, t);
            } else {
                throw new UnHandleException(t);
            }
        } catch (Throwable subThrowable) {
            if(channelErrorIt.hasNext()) {
                invokeNextChannelError(ctx, t);
            } else {
                defaultErrorHandlerLogger.warning("未处理的异常: "+ subThrowable.getMessage() +"\n" + ExceptionUtil.getStackTrace(subThrowable));
            }
        }
    }
}

package cn.t.ytten.core.channel;


import cn.t.ytten.core.exception.UnHandleException;
import cn.t.ytten.core.util.ExceptionUtil;
import cn.t.ytten.core.util.LoggingUtil;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

public class ChannelPipeline {

    private static final Logger logger = LoggingUtil.getLogger(ChannelPipeline.class);
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
        if(channelHandlerList.isEmpty()) {
            logger.warning("channelReady事件未处理");
        } else {
            this.channelReadyIt = channelHandlerList.iterator();
            this.invokeNextChannelReady(ctx);
        }
    }

    public void invokeNextChannelReady(ChannelContext ctx) {
        try {
            if(channelReadyIt.hasNext()) {
                channelReadyIt.next().ready(ctx);
            }
        } catch (Throwable t) {
            invokeChannelError(ctx, t);
        }
    }

    public void invokeChannelRead(ChannelContext ctx, Object msg) {
        if(channelHandlerList.isEmpty()) {
            logger.warning("channelRead事件未处理");
        } else {
            this.channelReadIt = channelHandlerList.iterator();
            this.invokeNextChannelRead(ctx, msg);
        }
    }

    public void invokeNextChannelRead(ChannelContext ctx, Object msg) {
        try {
            if(channelReadIt.hasNext()) {
                channelReadIt.next().read(ctx, msg);
            }
        } catch (Throwable t) {
            invokeChannelError(ctx, t);
        }
    }

    public void invokeChannelWrite(ChannelContext ctx, Object msg) {
        if(channelHandlerList.isEmpty()) {
            logger.warning("channelWrite事件未处理");
        } else {
            this.channelWriteIt = channelHandlerList.iterator();
            this.invokeNextChannelWrite(ctx, msg);
        }
    }

    public void invokeNextChannelWrite(ChannelContext ctx, Object msg) {
        try {
            if(channelWriteIt.hasNext()) {
                this.channelWriteIt.next().write(ctx, msg);
            }
        } catch (Throwable t) {
            invokeChannelError(ctx, t);
        }
    }

    public void invokeChannelClose(ChannelContext ctx) {
        if(channelHandlerList.isEmpty()) {
            logger.warning("channelClose事件未处理");
        } else {
            this.channelCloseIt = channelHandlerList.iterator();
            this.invokeNextChannelClose(ctx);
        }
    }

    public void invokeNextChannelClose(ChannelContext ctx) {
        try {
            if(channelCloseIt.hasNext()) {
                this.channelCloseIt.next().close(ctx);
            }
        } catch (Throwable t) {
            invokeChannelError(ctx, t);
        }
    }

    public void invokeChannelError(ChannelContext ctx, Throwable t) {
        if(channelHandlerList.isEmpty()) {
            logger.warning("channelError事件未处理");
        } else {
            this.channelErrorIt = channelHandlerList.iterator();
            this.invokeNextChannelError(ctx, t);
        }
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
                logger.warning("未处理的异常: "+ subThrowable.getMessage() +"\n" + ExceptionUtil.getStackTrace(subThrowable));
            }
        }
    }
}

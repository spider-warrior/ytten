package cn.t.ytten.core.channel;


import cn.t.ytten.core.exception.UnHandleException;
import cn.t.ytten.core.util.ExceptionUtil;
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

    public void invokeChannelReady(ChannelContext ctx) {
        if(channelHandlerList.isEmpty()) {
            logger.warning("channelReady事件未处理");
        } else {
            this.invokeNextChannelReady(null, ctx);
        }
    }

    public void invokeNextChannelReady(ChannelHandler handler, ChannelContext ctx) {
        try {
            if(handler == null) {
                channelHandlerList.get(0).ready(ctx);
            } else {
                for (int i = 0; i < channelHandlerList.size(); i++) {
                    if(channelHandlerList.get(i) == handler) {
                        int nextIndex = i + 1;
                        if(nextIndex < channelHandlerList.size()) {
                            channelHandlerList.get(nextIndex).ready(ctx);
                        }
                    }
                }
            }
        } catch (Throwable t) {
            invokeChannelError(ctx, t);
        }
    }

    public void invokeChannelRead(ChannelContext ctx, Object msg) {
        if(channelHandlerList.isEmpty()) {
            logger.warning("channelRead事件未处理");
        } else {
            this.invokeNextChannelRead(null, ctx, msg);
        }
    }

    public void invokeNextChannelRead(ChannelHandler handler, ChannelContext ctx, Object msg) {
        try {
            if(handler == null) {
                channelHandlerList.get(0).read(ctx, msg);
            } else {
                for (int i = 0; i < channelHandlerList.size(); i++) {
                    if(channelHandlerList.get(i) == handler) {
                        int nextIndex = i + 1;
                        if(nextIndex < channelHandlerList.size()) {
                            channelHandlerList.get(nextIndex).read(ctx, msg);
                        }
                    }
                }
            }
        } catch (Throwable t) {
            invokeChannelError(ctx, t);
        }
    }

    public void invokeChannelWrite(ChannelContext ctx, Object msg) {
        if(channelHandlerList.isEmpty()) {
            logger.warning("channelWrite事件未处理");
        } else {
            this.invokeNextChannelWrite(null, ctx, msg);
        }
    }

    public void invokeNextChannelWrite(ChannelHandler handler, ChannelContext ctx, Object msg) {
        try {
            if(handler == null) {
                channelHandlerList.get(0).write(ctx, msg);
            } else {
                for (int i = 0; i < channelHandlerList.size(); i++) {
                    if(channelHandlerList.get(i) == handler) {
                        int nextIndex = i + 1;
                        if(nextIndex < channelHandlerList.size()) {
                            channelHandlerList.get(nextIndex).write(ctx, msg);
                        }
                    }
                }
            }
        } catch (Throwable t) {
            invokeChannelError(ctx, t);
        }
    }

    public void invokeChannelClose(ChannelContext ctx) {
        if(channelHandlerList.isEmpty()) {
            logger.warning("channelClose事件未处理");
        } else {
            this.invokeNextChannelClose(null, ctx);
        }
    }

    public void invokeNextChannelClose(ChannelHandler handler, ChannelContext ctx) {
        try {
            if(handler == null) {
                channelHandlerList.get(0).close(ctx);
            } else {
                for (int i = 0; i < channelHandlerList.size(); i++) {
                    if(channelHandlerList.get(i) == handler) {
                        int nextIndex = i + 1;
                        if(nextIndex < channelHandlerList.size()) {
                            channelHandlerList.get(nextIndex).close(ctx);
                        }
                    }
                }
            }
        } catch (Throwable t) {
            invokeChannelError(ctx, t);
        }
    }

    public void invokeChannelError(ChannelContext ctx, Throwable t) {
        if(channelHandlerList.isEmpty()) {
            logger.warning("channelError事件未处理");
        } else {
            this.invokeNextChannelError(null, ctx, t);
        }
    }

    public void invokeNextChannelError(ChannelHandler handler, ChannelContext ctx, Throwable t) {
        ChannelHandler next = null;
        try {
            if(handler == null) {
                next = channelHandlerList.get(0);
                next.error(ctx, t);
            } else {
                for (int i = 0; i < channelHandlerList.size(); i++) {
                    if(channelHandlerList.get(i) == handler) {
                        int nextIndex = i + 1;
                        if(nextIndex < channelHandlerList.size()) {
                            next = channelHandlerList.get(nextIndex);
                            next.error(ctx, t);
                        }
                    }
                }
            }
            if(next == null) {
                throw new UnHandleException(t);
            }
        } catch (Throwable subThrowable) {
            if(subThrowable instanceof UnHandleException) {
                throw (UnHandleException)subThrowable;
            } else if(next != null) {
                invokeNextChannelError(next, ctx, subThrowable);
            } else {
                logger.warning("未处理的异常: "+ subThrowable.getMessage() +"\n" + ExceptionUtil.getStackTrace(subThrowable));
            }
        }
    }
}

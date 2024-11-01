package cn.t.ytten.core.channel;

import cn.t.ytten.core.eventloop.SingleThreadEventLoop;

import java.nio.channels.SelectableChannel;

public class ChannelContext {

    private final SelectableChannel selectableChannel;
    private final SingleThreadEventLoop eventLoop;
    private final ChannelPipeline pipeline = new ChannelPipeline();
    private final UnPooledHeapByteBuf readCache;
    private final UnPooledHeapByteBuf writeCache;

    public void invokeChannelReady() {
        pipeline.invokeChannelReady(this);
    }

    public void invokeNextChannelReady() {
        pipeline.invokeNextChannelReady(this);
    }

    public void invokeChannelRead() {
        pipeline.invokeChannelRead(this, readCache);
    }

    public void invokeNextChannelRead(Object msg) {
        pipeline.invokeNextChannelRead(this, msg);
    }
    public void invokeChannelWrite() {
        pipeline.invokeChannelWrite(this, writeCache);
    }

    public void invokeNextChannelWrite(Object msg) {
        pipeline.invokeNextChannelWrite(this, msg);
    }

    public void invokeChannelClose() {
        pipeline.invokeChannelClose(this);
    }

    public void invokeNextChannelClose() {
        pipeline.invokeNextChannelClose(this);
    }

    public void invokeChannelError(Throwable t) {
        pipeline.invokeChannelError(this, t);
    }

    public void invokeNextChannelError(Throwable t) {
        pipeline.invokeNextChannelError(this, t);
    }

    public SelectableChannel getSelectableChannel() {
        return selectableChannel;
    }

    public SingleThreadEventLoop getEventLoop() {
        return eventLoop;
    }

    public ChannelPipeline getPipeline() {
        return pipeline;
    }

    public UnPooledHeapByteBuf getReadCache() {
        return readCache;
    }

    public UnPooledHeapByteBuf getWriteCache() {
        return writeCache;
    }

    public ChannelContext(SelectableChannel selectableChannel, SingleThreadEventLoop eventLoop, UnPooledHeapByteBuf readCache, UnPooledHeapByteBuf writeCache) {
        this.selectableChannel = selectableChannel;
        this.eventLoop = eventLoop;
        this.readCache = readCache;
        this.writeCache = writeCache;
    }

    public static ChannelContext socketChannelContext(SelectableChannel selectableChannel, SingleThreadEventLoop eventLoop) {
        return new ChannelContext(selectableChannel, eventLoop, new UnPooledHeapByteBuf(), new UnPooledHeapByteBuf());
    }

    public static ChannelContext serverSocketChannelContext(SelectableChannel selectableChannel, SingleThreadEventLoop eventLoop) {
        return new ChannelContext(selectableChannel, eventLoop, null, null);
    }
}

package cn.t.ytten.core.channel;

import cn.t.ytten.core.eventloop.SingleThreadEventLoop;
import cn.t.ytten.core.exception.ChannelException;
import cn.t.ytten.core.util.ByteBufferUtil;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;

public class ChannelContext {

    private final SocketAddress remoteAddress;
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

    public void invokeChannelRead(Object msg) {
        pipeline.invokeChannelRead(this, msg);
    }

    public void invokeNextChannelRead(Object msg) {
        pipeline.invokeNextChannelRead(this, msg);
    }

    public void flush() throws IOException {
        if(selectableChannel instanceof SocketChannel) {
            if(writeCache.readableBytes() > 0) {
                ByteBuffer buffer = ByteBufferUtil.allocate();
                while (writeCache.readableBytes() > 0) {
                    writeCache.readBytes(buffer);
                    buffer.flip();
                    ((SocketChannel)selectableChannel).write(buffer);
                    buffer.clear();
                }
            }
        }
    }

    public void invokeChannelWrite(Object msg) {
        pipeline.invokeChannelWrite(this, msg);
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

    public SelectionKey register(Selector selector, int ops) {
        return this.register(selector, ops, null);
    }

    public SelectionKey register(Selector selector, int ops, Object attachment) {
        try {
            return this.selectableChannel.register(selector, ops, attachment);
        } catch (ClosedChannelException e) {
            throw new ChannelException(e);
        }
    }

    public int read(ByteBuffer buffer) throws IOException {
        return ((SocketChannel)selectableChannel).read(buffer);
    }

    public void close() throws IOException {
        try {
            selectableChannel.close();
        } finally {
            invokeChannelClose();
        }
    }

    public SocketAddress remoteAddress() throws IOException {
        return remoteAddress;
    }

    public ChannelContext(SelectableChannel selectableChannel, SocketAddress remoteAddress, SingleThreadEventLoop eventLoop, UnPooledHeapByteBuf readCache, UnPooledHeapByteBuf writeCache) {
        this.selectableChannel = selectableChannel;
        this.remoteAddress = remoteAddress;
        this.eventLoop = eventLoop;
        this.readCache = readCache;
        this.writeCache = writeCache;
    }

    public static ChannelContext socketChannelContext(SelectableChannel selectableChannel, SocketAddress remoteAddress, SingleThreadEventLoop eventLoop) {
        return new ChannelContext(selectableChannel, remoteAddress, eventLoop, new UnPooledHeapByteBuf(), new UnPooledHeapByteBuf());
    }

    public static ChannelContext serverSocketChannelContext(SelectableChannel selectableChannel, SingleThreadEventLoop eventLoop) {
        return new ChannelContext(selectableChannel, null, eventLoop, null, null);
    }
}

package cn.t.ytten.core.channel;

public class ChannelContext {

    private final ChannelPipeline pipeline = new ChannelPipeline();
    private final UnPooledHeapByteBuf readCache = new UnPooledHeapByteBuf();
    private final UnPooledHeapByteBuf writeCache = new UnPooledHeapByteBuf();

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

    public ChannelPipeline getPipeline() {
        return pipeline;
    }

    public UnPooledHeapByteBuf getReadCache() {
        return readCache;
    }
}

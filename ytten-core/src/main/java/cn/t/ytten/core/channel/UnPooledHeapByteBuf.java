package cn.t.ytten.core.channel;

import cn.t.ytten.core.exception.DecodeException;
import cn.t.ytten.core.util.HeapByteBufUtil;

import java.nio.ByteBuffer;

public class UnPooledHeapByteBuf {

    private static final int defaultSize = 2048;

    private static final int minExpandEachTimeSize = 1024;
    private static final int maxExpandEachTimeSize = 1024 * 1024;
    private static final int maxCacheBufSize = 1024 * 1024 * 4;

    private byte[] buf;
    //readerIndex <= writerIndex < buf.length
    private int readerIndex;
    private int writerIndex;
    private int lastExpandSize;

    public UnPooledHeapByteBuf skipBytes(int length) {
        readerIndex += length;
        return this;
    }

    public byte readByte() {
        return HeapByteBufUtil.getByte(buf, readerIndex++);
    }

    public boolean readBoolean() {
        return readByte() != 0;
    }

    public short readShort() {
        short v = HeapByteBufUtil.getShort(buf, readerIndex);
        readerIndex += 2;
        return v;
    }

    public int readInt() {
        int v = HeapByteBufUtil.getInt(buf, readerIndex);
        readerIndex += 4;
        return v;
    }

    public long readLong() {
        long v = HeapByteBufUtil.getLong(buf, readerIndex);
        readerIndex += 8;
        return v;
    }

    public void readBytes(byte[] dst, int dstIndex, int length) {
        getBytes(readerIndex, dst, dstIndex, length);
        readerIndex += length;
    }

    public void readBytes(byte[] dst) {
        readBytes(dst, 0, dst.length);
    }

    public void readBytes(ByteBuffer dst) {
        int writeLength = Math.min(readableBytes(), dst.remaining());
        dst.put(buf, readerIndex, writeLength);
        readerIndex += writeLength;
    }

    public void getBytes(int index, byte[] dst, int dstIndex, int length) {
        System.arraycopy(buf, index, dst, dstIndex, length);
    }

    public char readChar() {
        return (char) readShort();
    }

    public float readFloat() {
        return Float.intBitsToFloat(readInt());
    }

    public double readDouble() {
        return Double.longBitsToDouble(readLong());
    }

    public UnPooledHeapByteBuf writeBoolean(boolean value) {
        return writeByte(value ? (byte)1 : (byte)0);
    }

    public UnPooledHeapByteBuf writeByte(byte value) {
        ensureWritable(1);
        buf[writerIndex++] = value;
        return this;
    }

    public UnPooledHeapByteBuf writeShort(int value) {
        ensureWritable(2);
        HeapByteBufUtil.setShort(buf, writerIndex, value);
        writerIndex += 2;
        return this;
    }

    public UnPooledHeapByteBuf writeInt(int value) {
        ensureWritable(4);
        HeapByteBufUtil.setInt(buf, writerIndex, value);
        writerIndex += 4;
        return this;
    }

    public void writeLong(long value) {
        ensureWritable(8);
        HeapByteBufUtil.setLong(buf, writerIndex, value);
        writerIndex += 8;
    }

    public UnPooledHeapByteBuf writeChar(int value) {
        return writeShort(value);
    }

    public UnPooledHeapByteBuf writeFloat(float value) {
        return writeInt(Float.floatToRawIntBits(value));
    }

    public void writeDouble(double value) {
        writeLong(Double.doubleToRawLongBits(value));
    }

    public UnPooledHeapByteBuf writeBytes(byte[] src, int srcIndex, int length) {
        ensureWritable(length);
        System.arraycopy(src, srcIndex, buf, writerIndex, length);
        writerIndex += length;
        return this;
    }

    public UnPooledHeapByteBuf writeBytes(byte[] src) {
        return writeBytes(src, 0, src.length);
    }

    public void writeBytes(ByteBuffer src) {
        int length = src.remaining();
        ensureWritable(length);
        src.get(buf, writerIndex, src.remaining());
        writerIndex += length;
    }

    public int writerIndex() {
        return writerIndex;
    }

    public int readerIndex() {
        return readerIndex;
    }

    public void readerIndex(int readerIndex) {
        this.readerIndex = readerIndex;
    }

    public void writerIndex(int writerIndex) {
        this.writerIndex = writerIndex;
    }

    public int readableBytes() {
        return writerIndex - readerIndex;
    }

    final void ensureWritable(int writeBytes) {
        //剩余空间
        int remainWritableBytes = buf.length - writerIndex;
        if(writeBytes > remainWritableBytes) {
            //最大可用空间
            int maxRemainWritableBytes = remainWritableBytes + readerIndex;
            //如果压缩后空间可容纳写入内容
            if(maxRemainWritableBytes >= writeBytes) {
                compact(readerIndex, writerIndex - readerIndex);
            } else {
                if(buf.length > maxCacheBufSize) {
                    throw new DecodeException("cache buf too large");
                }
                //扩展字段
                int expandSize = writeBytes - maxRemainWritableBytes;
                if(expandSize < maxExpandEachTimeSize) {
                    //最小扩容检查
                    if(expandSize < minExpandEachTimeSize) {
                        expandSize = minExpandEachTimeSize;
                    } else {
                        expandSize = minExpandEachTimeSize * 2;
                    }
                    //保证扩容大小是单调递增
                    if(expandSize < lastExpandSize) {
                        expandSize = lastExpandSize;
                    } else {
                        lastExpandSize = expandSize;
                    }
                }
                System.out.println("缓存buf扩展, 当前大小: " + buf.length + ", 扩展大小: " + expandSize);
                int newCapacity = buf.length + expandSize;
                byte[] newBuf = new byte[newCapacity];
                System.arraycopy(buf, readerIndex, newBuf, 0, writerIndex - readerIndex);
                buf = newBuf;
            }
        }
    }

    private void compact(int startIndex, int count) {
        for (int i = startIndex; i < startIndex + count; i++) {
            buf[i-startIndex] = buf[i];
        }
        //clear(有writerIndex做边界限制，可以不用清理)
//        for (int i = count; i < startIndex + count; i++) {
//            buf[i] = 0;
//        }
        readerIndex = 0;
        writerIndex = count;
    }

    public UnPooledHeapByteBuf() {
        this.buf = new byte[defaultSize];
        this.readerIndex = 0;
        this.writerIndex = 0;
        this.lastExpandSize = 0;
    }
}

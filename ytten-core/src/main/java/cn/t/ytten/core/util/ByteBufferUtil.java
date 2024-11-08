package cn.t.ytten.core.util;

import java.nio.ByteBuffer;

public class ByteBufferUtil {

    private static final ThreadLocal<ByteBuffer> threadLocal = new ThreadLocal<>();

    public static ByteBuffer allocate() {
        ByteBuffer buffer = threadLocal.get();
        if (buffer == null) {
            buffer = ByteBuffer.allocateDirect(1024 * 1024);
            threadLocal.set(buffer);
        } else {
            buffer.clear();
        }
        return buffer;
    }
}
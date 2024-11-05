package cn.t.ytten.core.eventloop;

import cn.t.ytten.core.channel.ChannelContext;
import cn.t.ytten.core.util.ExceptionUtil;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Comparator;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.function.Consumer;

public class SingleThreadEventLoop implements Runnable {

    private static final int defaultSelectTimeInMills = 3000;
    private static final int defaultIoLoopTimes = 5;
    private final ByteBuffer tmp = ByteBuffer.allocate(1024*1024);
    private final BlockingQueue<EventLoopTask<?>> inTimeTask = new LinkedBlockingQueue<>();
    private final PriorityBlockingQueue<EventLoopDelayTask> delayTaskQueue = new PriorityBlockingQueue<>(10, Comparator.comparingLong(EventLoopDelayTask::getExecuteTimePointInMills));
    private final Selector selector;
    private final Thread thread;
    private volatile int state = EventLoopState.NOT_STARTED;

    public boolean inEventLoop() {
        return Thread.currentThread() == this.thread;
    }

    @Override
    public void run() {
        state = EventLoopState.STARTED;
        try {
            while (state == EventLoopState.STARTED) {
                int count = selector.select(nextSelectTimeout());
                if(count > 0) {
                    Iterator<SelectionKey> it = selector.selectedKeys().iterator();
                    while (it.hasNext()) {
                        SelectionKey key = it.next();
                        it.remove();
                        if(key.isConnectable()) {

                        } else if(key.isAcceptable()) {

                        }
                        if(key.isWritable()) {

                        }
                        if(key.isReadable()) {
                            SocketChannel socketChannel = (SocketChannel)key.channel();
                            ChannelContext ctx = (ChannelContext)key.attachment();
                            int lastReadLength = 0;
                            for (int i = 0; i < defaultIoLoopTimes; i++) {
                                lastReadLength = socketChannel.read(tmp);
                                if (lastReadLength > 0) {
                                    tmp.flip();
                                    ctx.getReadCache().writeBytes(tmp);
                                    tmp.clear();
                                    if(lastReadLength < tmp.capacity()) {
                                        //消息已读完
                                        break;
                                    }
                                } else {
                                    break;
                                }
                            }
                            if(lastReadLength > -1) {
                                ctx.invokeChannelRead();
                            } else {
                                //连接已关闭
                                ctx.invokeChannelClose();
                            }
                        }
                    }
                }
            }
        } catch (Throwable t) {
            System.out.println(ExceptionUtil.getErrorMessage(t));
        } finally {
            try { selector.close();} catch (Exception ignore) {};
        }
    }

    private long nextSelectTimeout() {
        EventLoopDelayTask eventLoopDelayTask = delayTaskQueue.peek();
        if(eventLoopDelayTask == null) {
            return defaultSelectTimeInMills;
        } else {
            return eventLoopDelayTask.getExecuteTimePointInMills() - System.currentTimeMillis();
        }
    }

    private void runInTimeTask() {
        while (true) {
            EventLoopTask<?> eventLoopTask = inTimeTask.poll();
            if(eventLoopTask == null) {
                break;
            }
            eventLoopTask.run();
        }
    }

    private void runDelayTasK() {
        while (true) {
            EventLoopDelayTask delayTask = delayTaskQueue.poll();
            if(delayTask == null || delayTask.getExecuteTimePointInMills() > System.currentTimeMillis()) {
                break;
            }
            delayTask.getTask().run();
        }
    }

    public <V> TaskWorkChain<V, V> addTask(Callable<V> callable) {
        return this.addTask(callable, null);
    }

    public <V> TaskWorkChain<V, V> addTask(Callable<V> callable, Consumer<Throwable> errorHandler) {
        TaskWorkChain<V, V> taskWorkChain = TaskWorkChain.newInstance();
        inTimeTask.add(new EventLoopTask<>(callable, errorHandler, taskWorkChain));
        return taskWorkChain;
    }

    public void addDelayTask(EventLoopDelayTask delayTask) {
        delayTaskQueue.add(delayTask);
    }

    public void nextLoop() {
        this.selector.wakeup();
    }

    public Selector getSelector() {
        return selector;
    }

    public SingleThreadEventLoop(String name) throws IOException {
        this.selector = Selector.open();
        this.thread = new Thread(this, name);
        this.thread.start();
    }
}

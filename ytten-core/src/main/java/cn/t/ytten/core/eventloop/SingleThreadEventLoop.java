package cn.t.ytten.core.eventloop;

import cn.t.ytten.core.channel.ChannelContext;
import cn.t.ytten.core.util.ExceptionUtil;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Comparator;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;

public class SingleThreadEventLoop implements Runnable {

    private static final int defaultSelectTimeInMills = 3000;
    private static final int defaultIoLoopTimes = 5;
    private final ByteBuffer tmp = ByteBuffer.allocate(1024*1024);
    private final BlockingQueue<ExecuteChain<?>> inTimeTask = new LinkedBlockingQueue<>();
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
                            ChannelContext ctx = (ChannelContext)key.attachment();
                            ServerSocketChannel serverSocketChannel = (ServerSocketChannel)ctx.getSelectableChannel();
                            SocketChannel socketChannel = serverSocketChannel.accept();
                            ctx.invokeChannelRead(socketChannel);
                        }
                        if(key.isWritable()) {

                        }
                        if(key.isReadable()) {
                            ChannelContext ctx = (ChannelContext)key.attachment();
                            int lastReadLength = 0;
                            for (int i = 0; i < defaultIoLoopTimes; i++) {
                                lastReadLength = ((SocketChannel)ctx.getSelectableChannel()).read(tmp);
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
                                ctx.invokeChannelRead(ctx.getReadCache());
                            } else {
                                //连接已关闭
                                ctx.invokeChannelClose();
                            }
                        }
                    }
                }
                //及时任务
                runInTimeTask();
                //延时任务
                runDelayTasK();
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
            ExecuteChain<?> eventLoopTask = inTimeTask.poll();
            if(eventLoopTask == null) {
                break;
            }
            eventLoopTask.execute();
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


    public <V> void addTask(ExecuteChain<V> chain) {
        inTimeTask.add(chain);
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

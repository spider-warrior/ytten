package cn.t.ytten.core.eventloop;

import cn.t.ytten.core.channel.ChannelContext;
import cn.t.ytten.core.util.ExceptionUtil;
import cn.t.ytten.core.util.LoggingUtil;

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
import java.util.logging.Logger;

public class SingleThreadEventLoop implements Runnable {

    private static final Logger logger = LoggingUtil.getLogger(SingleThreadEventLoop.class);

    private static final int defaultSelectTimeInMills = 3000;
    private static final int defaultIoLoopTimes = 5;
    private final ByteBuffer tmp = ByteBuffer.allocate(1024*1024);
    private final BlockingQueue<Runnable> inTimeTask = new LinkedBlockingQueue<>();
    private final PriorityBlockingQueue<EventLoopDelayTask> delayTaskQueue = new PriorityBlockingQueue<>(10, Comparator.comparingLong(EventLoopDelayTask::getRate));
    private final String name;
    private final Selector selector;
    private volatile int state = EventLoopState.NOT_STARTED;

    @Override
    public void run() {
        state = EventLoopState.STARTED;
        try {
            while (state == EventLoopState.STARTED) {
                //及时任务
                runInTimeTask();
                //延时任务
                long nextTaskExecuteTimePoint = runDelayTasK();
                int  count;
                if(nextTaskExecuteTimePoint < 0) {
                    count = selector.select(defaultSelectTimeInMills);
                } else {
                    count = selector.select(nextTaskExecuteTimePoint - System.currentTimeMillis());
                }
                if(count > 0) {
                    Iterator<SelectionKey> it = selector.selectedKeys().iterator();
                    while (it.hasNext()) {
                        SelectionKey key = it.next();
                        it.remove();
                        if(key.isConnectable()) {
                            SocketChannel socketChannel = ((SocketChannel)key.channel());
                            ChannelContext ctx = (ChannelContext)key.attachment();
                            if(socketChannel.finishConnect()) {
                                ctx.invokeChannelReady();
                                ctx.register(selector, SelectionKey.OP_READ);
                            } else {
                                System.out.println("not connected yet...");
                            }
                        } else if(key.isAcceptable()) {
                            SocketChannel socketChannel = ((ServerSocketChannel)key.channel()).accept();
                            ((ChannelContext)key.attachment()).invokeChannelRead(socketChannel);
                        }
                        if(key.isWritable()) {
                            ChannelContext ctx = (ChannelContext)key.attachment();
                            ctx.invokeChannelWrite(ctx.getWriteCache());
                        }
                        if(key.isReadable()) {
                            ChannelContext ctx = (ChannelContext)key.attachment();
                            int lastReadLength = 0;
                            for (int i = 0; i < defaultIoLoopTimes; i++) {
                                try {
                                    lastReadLength = ctx.read(tmp);
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
                                } catch (Throwable t) {
                                    logger.warning("读取消息异常, " + t);
                                    //设定标志关闭连接
                                    lastReadLength = -1;
                                    break;
                                }
                            }
                            if(lastReadLength > -1) {
                                ctx.invokeChannelRead(ctx.getReadCache());
                            } else {
                                key.cancel();
                                //连接已关闭
                                ctx.invokeChannelClose();
                            }
                        }
                    }
                }
            }
        } catch (Throwable t) {
            logger.warning("未处理异常: " + ExceptionUtil.getErrorMessage(t));
        } finally {
//            try { selector.close();} catch (Exception ignore) {};
        }
    }

    private void runInTimeTask() {
        while (true) {
            Runnable runnable = inTimeTask.poll();
            if(runnable == null) {
                break;
            }
            runnable.run();
        }
    }

    private long runDelayTasK() {
        while (true) {
            EventLoopDelayTask delayTask = delayTaskQueue.poll();
            if(delayTask == null) {
                return -1;
            } else if(delayTask.getRunAt() > System.currentTimeMillis()) {
                return delayTask.getRunAt();
            } else {
                delayTask.getRunnable().run();
                if(delayTask.isRepeat()) {
                    delayTask.markNextRunAt();
                    delayTaskQueue.add(delayTask);
                }
            }
        }
    }

    public void addTask(Runnable runnable) {
        inTimeTask.add(runnable);
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

    public String getName() {
        return name;
    }

    public void stop() {
        this.state = EventLoopState.SHUTDOWN;
    }

    public SingleThreadEventLoop(String name) throws IOException {
        this.name = name;
        this.selector = Selector.open();
    }
}

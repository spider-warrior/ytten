package cn.t.ytten.metricexposer.server;

import cn.t.ytten.core.ServerBootstrap;
import cn.t.ytten.core.eventloop.SingleThreadEventLoop;

public class MetricExposerServer {
    private final SingleThreadEventLoop acceptLoop;
    private final SingleThreadEventLoop ioLoop;

    public void start() {
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.start(5566, acceptLoop, ioLoop, new ExposerServerChannelInitializer());
    }

    public void stop() {
        acceptLoop.stop();
        ioLoop.stop();
    }

    public MetricExposerServer(SingleThreadEventLoop acceptLoop, SingleThreadEventLoop ioLoop) {
        this.acceptLoop = acceptLoop;
        this.ioLoop = ioLoop;
    }
}

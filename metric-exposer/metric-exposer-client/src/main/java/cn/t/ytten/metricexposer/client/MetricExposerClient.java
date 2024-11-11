package cn.t.ytten.metricexposer.client;

import cn.t.ytten.core.ClientBootstrap;
import cn.t.ytten.core.eventloop.SingleThreadEventLoop;

public class MetricExposerClient {
    private final String host;
    private final int port;
    private final SingleThreadEventLoop ioLoop;
    public void start() {
        ClientBootstrap bootstrap = new ClientBootstrap();
        bootstrap.start(host, port, ioLoop, new ExposerClientChannelInitializer());
    }

    public MetricExposerClient(String host, int port, SingleThreadEventLoop ioLoop) {
        this.host = host;
        this.port = port;
        this.ioLoop = ioLoop;
    }
}

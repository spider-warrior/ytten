package cn.t.ytten.metricexposer.server.test;

import cn.t.ytten.core.eventloop.SingleThreadEventLoop;
import cn.t.ytten.metricexposer.server.MetricExposerServer;

import java.io.IOException;

public class MetricExposerServerTest {
    public static void main(String[] args) throws IOException {
        SingleThreadEventLoop acceptLoop = new SingleThreadEventLoop("accept");
        SingleThreadEventLoop ioLoop = new SingleThreadEventLoop("io");
        MetricExposerServer exposerServer = new MetricExposerServer(acceptLoop, ioLoop);
        exposerServer.start();
    }
}

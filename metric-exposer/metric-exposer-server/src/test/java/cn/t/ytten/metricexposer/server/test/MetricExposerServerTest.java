package cn.t.ytten.metricexposer.server.test;

import cn.t.ytten.core.eventloop.SingleThreadEventLoop;
import cn.t.ytten.metricexposer.server.MetricExposerServer;

import java.io.IOException;

public class MetricExposerServerTest {
    public static void main(String[] args) throws IOException {
        SingleThreadEventLoop acceptLoop = new SingleThreadEventLoop("accept");
        MetricExposerServer exposerServer = new MetricExposerServer(acceptLoop, acceptLoop);
        exposerServer.start();
    }
}

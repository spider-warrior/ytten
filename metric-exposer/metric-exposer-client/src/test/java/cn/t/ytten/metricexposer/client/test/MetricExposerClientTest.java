package cn.t.ytten.metricexposer.client.test;

import cn.t.ytten.core.eventloop.SingleThreadEventLoop;
import cn.t.ytten.metricexposer.client.MetricExposerClient;

import java.io.IOException;

public class MetricExposerClientTest {
    public static void main(String[] args) throws IOException {
        String host = "127.0.0.1";
        int port = 5566;
        SingleThreadEventLoop ioLoop = new SingleThreadEventLoop("ioLoop");
        MetricExposerClient metricExposerClient = new MetricExposerClient(host, port, ioLoop);
        metricExposerClient.start();
    }
}

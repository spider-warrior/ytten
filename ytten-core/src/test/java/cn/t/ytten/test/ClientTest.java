package cn.t.ytten.test;

import cn.t.ytten.core.ClientBootstrap;
import cn.t.ytten.core.eventloop.SingleThreadEventLoop;

public class ClientTest {
    public static void main(String[] args) throws Exception {
        ClientBootstrap clientBootstrap = new ClientBootstrap();
        clientBootstrap.start("127.0.0.1", 5566, new SingleThreadEventLoop("io-event-loop"));
    }
}

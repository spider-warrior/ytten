package cn.t.ytten.test;

import cn.t.ytten.core.ServerBootstrap;
import cn.t.ytten.core.eventloop.SingleThreadEventLoop;

import java.io.IOException;

public class ServerTest {
    public static void main(String[] args) throws IOException {
        SingleThreadEventLoop eventLoop = new SingleThreadEventLoop("bossGroup");
        ServerBootstrap serverBootstrap = new ServerBootstrap(eventLoop, eventLoop);
        serverBootstrap.start(5566);
    }
}
package cn.t.ytten.core.channel;

import java.nio.channels.Channel;

@FunctionalInterface
public interface Channelnitializer<C extends Channel> {
    void initChannel(ChannelContext ctx, C c) throws Exception;
}

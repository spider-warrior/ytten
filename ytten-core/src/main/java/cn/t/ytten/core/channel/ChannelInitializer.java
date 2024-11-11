package cn.t.ytten.core.channel;

import java.nio.channels.Channel;

@FunctionalInterface
public interface ChannelInitializer {
    void initChannel(ChannelContext ctx, Channel channel);
}

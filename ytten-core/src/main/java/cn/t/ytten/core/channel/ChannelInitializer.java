package cn.t.ytten.core.channel;

import java.nio.channels.SelectableChannel;

@FunctionalInterface
public interface ChannelInitializer {
    void initChannel(ChannelContext ctx, SelectableChannel channel);
}

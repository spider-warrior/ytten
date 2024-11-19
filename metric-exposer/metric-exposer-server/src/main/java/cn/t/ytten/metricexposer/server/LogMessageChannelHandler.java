package cn.t.ytten.metricexposer.server;

import cn.t.ytten.core.channel.ChannelContext;
import cn.t.ytten.core.channel.ChannelHandler;
import cn.t.ytten.core.util.LoggingUtil;

import java.util.Objects;
import java.util.logging.Logger;

public class LogMessageChannelHandler implements ChannelHandler {

    private static final Logger logger = LoggingUtil.getLogger(LogMessageChannelHandler.class);

    @Override
    public void read(ChannelContext ctx, Object msg) throws Exception {
        logger.info(Objects.toString(msg));
    }
}

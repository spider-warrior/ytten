package cn.t.ytten.core.logging;

import java.util.logging.Logger;

public class LoggingConfig {
    static {
        // 获取根Logger
        Logger rootLogger = Logger.getLogger("");
        // 移除默认的处理器
        rootLogger.setUseParentHandlers(false);
        rootLogger.addHandler(new StandardConsoleHandler());
    }
}

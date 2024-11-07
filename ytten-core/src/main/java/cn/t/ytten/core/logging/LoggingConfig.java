package cn.t.ytten.core.logging;

import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class LoggingConfig {
    static {
        System.getProperties().putIfAbsent("java.util.logging.config.class", "cn.t.ytten.core.logging.LoggingConfig");
        // 获取根Logger
        Logger rootLogger = LogManager.getLogManager().getLogger("");
        // 移除默认的处理器
        rootLogger.setUseParentHandlers(false);
        rootLogger.addHandler(new StandardConsoleHandler());
    }

    public static void main(String[] args) {
        Logger logger = Logger.getLogger("abc");
        logger.log(Level.INFO, "hello world");
    }
}

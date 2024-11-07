package cn.t.ytten.core.util;

import java.util.logging.Logger;

public class LoggingUtil {
    static {
        System.getProperties().putIfAbsent("java.util.logging.config.class", "cn.t.ytten.core.logging.LoggingConfig");
    }

    public static Logger getLogger(Class clazz) {
        return Logger.getLogger(clazz.getName());
    }
}

package cn.t.ytten.core.logging;

import java.util.logging.LogRecord;
import java.util.logging.StreamHandler;

/**
 * @see java.util.logging.ConsoleHandler
 */
public class StandardConsoleHandler extends StreamHandler {
    public StandardConsoleHandler() {
        super(System.out, new StandardLoggingFormatter());
    }

    @Override
    public void publish(LogRecord record) {
        super.publish(record);
        flush();
    }

    @Override
    public void close() {
        flush();
    }
}

package cn.t.ytten.core.logging;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class StandardLoggingFormatter extends Formatter {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    @Override
    public String format(LogRecord record) {
        return LocalDateTime.now().format(formatter) + " [" + Thread.currentThread().getName() + "] " + record.getLevel() + " " + record.getLoggerName() + " - " + record.getMessage() + "\n";
    }
}

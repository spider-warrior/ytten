package cn.t.ytten.metricexposer.common.util;

import cn.t.ytten.core.util.LoggingUtil;
import cn.t.ytten.metricexposer.common.constants.SystemConstants;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

public class CommandUtil {

    private static final Logger logger = LoggingUtil.getLogger(CommandUtil.class);

    private static final AtomicInteger count = new AtomicInteger(1);
    private static final BlockingQueue<ProcessBuilder> processBuilderQueue = new LinkedBlockingQueue<>();

    public static List<String> buildNativeCommand(String command) {
        List<String> commandItems = new ArrayList<>(3);
        if(SystemConstants.IS_WINDOWS) {
            commandItems.add("cmd.exe");
            commandItems.add("/c");
        } else {
            commandItems.add("bash");
            commandItems.add("-c");
        }
        commandItems.add(command);
        return commandItems;
    }

    public static String execute(String command) {
        List<String> nativeCommand = buildNativeCommand(command);
        ProcessBuilder processBuilder = processBuilderQueue.poll();
        if(processBuilder == null) {
            processBuilder = new ProcessBuilder(nativeCommand);
            processBuilder.redirectErrorStream(true);
            logger.info("新建ProcessBuilder, index: " + count.getAndIncrement());
        } else {
            processBuilder.command(nativeCommand);
        }
        try {
            Process process = processBuilder.start();
            StringBuilder builder = new StringBuilder();
            try(BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream(), SystemConstants.CONSOLE_ENCODING))) {
                bufferedReader.lines().forEach(line -> builder.append(line).append(System.lineSeparator()));
            }
            int exitCode = process.waitFor();
            if(exitCode != 0) {
                throw new RuntimeException("命令执行失败,exitCode: " + exitCode + ",命令: " + command);
            }
            return builder.toString();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        } finally {
            boolean success = processBuilderQueue.offer(processBuilder);
            if(!success) {
                System.out.println("processBuilderQueue.offer(processBuilder) failed, processBuilderQueue.size: " + processBuilderQueue.size());
            }
        }
    }
}

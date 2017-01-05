package io.openexchange.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public class ExecutorsUtil {
    private final static Logger logger = LoggerFactory.getLogger(ExecutorsUtil.class);

    public static void shutdownExecutorService(ExecutorService es) {
        es.shutdown();
        try {
            if (!es.awaitTermination(60, TimeUnit.MILLISECONDS)) {
                es.shutdownNow();
                if (!es.awaitTermination(30, TimeUnit.MILLISECONDS)) {
                    logger.error("Pool is not shutting down!!!");
                }
            }
        } catch (InterruptedException e) {
            logger.error(e.getMessage(), e);
        }
    }
}

package io.openexchange.utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public class ExecutorsUtil {

    public static void shutdownExecutorService(ExecutorService es) {
        es.shutdown();
        try {
            if (!es.awaitTermination(60, TimeUnit.MILLISECONDS)) {
                es.shutdownNow();
                if (!es.awaitTermination(30, TimeUnit.MILLISECONDS)) {
                    System.err.println("Pool is not shutting down!!!");
                }
            }
        } catch (InterruptedException e) {
            // Ignore
        }
    }
}

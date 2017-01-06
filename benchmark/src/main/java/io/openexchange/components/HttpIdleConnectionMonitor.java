package io.openexchange.components;

import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static io.openexchange.utils.ExecutorsUtil.shutdownExecutorService;

@Component
public class HttpIdleConnectionMonitor {
    private final static Logger logger = LoggerFactory.getLogger(HttpIdleConnectionMonitor.class);

    private final PoolingHttpClientConnectionManager cm;
    private final ScheduledExecutorService monitor;

    @Autowired
    public HttpIdleConnectionMonitor(PoolingHttpClientConnectionManager cm) {
        this.monitor = Executors.newScheduledThreadPool(1);
        this.cm = cm;
    }

    @PostConstruct
    private void init() {
        logger.info("Starting idle connections monitor thread...");
        monitor.scheduleAtFixedRate(() -> {
                    logger.debug("Try evict expired connections");
                    cm.closeExpiredConnections();
                    cm.closeIdleConnections(30, TimeUnit.SECONDS);
                    logger.debug("Expired connections have been evicted");
                },
                5000,
                5000,
                TimeUnit.MILLISECONDS
        );
        logger.info("Idle connections monitor thread has been started");
    }

    @PreDestroy
    private void destroy() {
        logger.info("Stopping idle connections monitor thread...");
        shutdownExecutorService(monitor);
        logger.info("Idle connections monitor thread has been stopped");
    }
}

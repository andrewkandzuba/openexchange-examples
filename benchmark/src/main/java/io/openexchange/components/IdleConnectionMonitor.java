package io.openexchange.components;

import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.concurrent.TimeUnit;

@Component
public class IdleConnectionMonitor {
    private final static Logger logger = LoggerFactory.getLogger(IdleConnectionMonitor.class);

    private final PoolingHttpClientConnectionManager cm;
    private final IdleConnectionMonitorThread monitor;

    @Autowired
    public IdleConnectionMonitor(PoolingHttpClientConnectionManager cm) {
        monitor = new IdleConnectionMonitorThread();
        this.cm = cm;
    }

    @PostConstruct
    private void init(){
        logger.info("Starting idle connections monitor thread...");
        monitor.start();
        logger.info("Idle connections monitor thread has been started");
    }

    @PreDestroy
    private void destroy(){
        logger.info("Stopping idle connections monitor thread...");
        monitor.shutdown();
        logger.info("Idle connections monitor thread has been stopped");
    }

    private class IdleConnectionMonitorThread extends Thread {
        private volatile boolean shutdown;

        @Override
        public void run() {
            logger.info("Enters idle connection idleMonitor");
            try {
                while (!shutdown) {
                    synchronized (this) {
                        wait(5000);
                        logger.debug("Try evict expired connections");
                        cm.closeExpiredConnections();
                        cm.closeIdleConnections(30, TimeUnit.SECONDS);
                    }
                }
            } catch (InterruptedException e) {
                logger.error(e.getMessage(), e);
            }
            logger.info("Exits idle connection idleMonitor");
        }

        void shutdown() {
            shutdown = true;
            synchronized (this) {
                notifyAll();
            }
        }
    }
}

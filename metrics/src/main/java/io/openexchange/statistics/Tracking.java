package io.openexchange.statistics;

import io.openexchange.statistics.metrics.Request;
import io.openexchange.statistics.metrics.Server;
import io.openexchange.utils.ExecutorsUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class Tracking {
    private static final Logger logger = LoggerFactory.getLogger(Tracking.class);
    private final ExecutorService es;
    private final Map<InetSocketAddress, Server> stats;

    public Tracking() {
        this.es = Executors.newSingleThreadExecutor();
        this.stats = new ConcurrentHashMap<>();
    }

    public void log(final InetSocketAddress hostAndPort, Request rs){
        es.submit(() -> {
            Server current = stats.putIfAbsent(hostAndPort, new Server(hostAndPort));
            current.update(rs);
        });
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("\n");
        for(Server rs : stats.values()) {
            sb.append(rs.toString()).append("\n");
        }
        return sb.toString();
    }

    @PreDestroy
    private void destroy(){
        logger.debug("Stopping statistics tracking service...");
        ExecutorsUtil.shutdownExecutorService(es);
        logger.debug("Statistics tracking service has been stopped.");
    }
}

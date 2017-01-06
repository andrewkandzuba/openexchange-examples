package io.openexchange.statistics;

import io.openexchange.utils.ExecutorsUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class StatisticsTrackingService {
    private static final Logger logger = LoggerFactory.getLogger(StatisticsTrackingService.class);
    private final ExecutorService es;
    private final Map<URI, RequestStatistic> statistics;

    public StatisticsTrackingService() {
        this.es = Executors.newSingleThreadExecutor();
        this.statistics = new ConcurrentHashMap<>();
    }

    public void log(final URI uri, final long longevity, final long wait, final boolean success){
        es.submit(() -> {
            RequestStatistic current = statistics.getOrDefault(uri, new RequestStatistic(uri));
            current.total += 1;
            current.activeTime += longevity;
            current.waitTime += wait;
            if(success) current.success += 1;
            statistics.put(uri, current);
        });
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("\n");
        for(RequestStatistic rs : statistics.values()) {
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

    private static class RequestStatistic {
        URI uri;
        long success = 0;
        long total = 0;
        long activeTime = 0;
        long waitTime = 0;

        private RequestStatistic(URI uri) {
            this.uri = uri;
        }

        @Override
        public String toString() {
            return "RequestStatistic{" +
                    "uri='" + uri + '\'' +
                    ", success=" + success +
                    ", failures=" + (total - success) +
                    ", total=" + total +
                    ", total request time=" + activeTime +
                    ", avg. request time=" +(double) activeTime / (double) total  + " mls." +
                    ", avg. client time=" + (double) waitTime / (double) total + " mls." +
                    ", avg. request per mls.=" + (double) total / (double) waitTime +
                    '}';
        }
    }
}

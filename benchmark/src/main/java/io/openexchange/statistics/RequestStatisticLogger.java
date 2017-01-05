package io.openexchange.statistics;

import io.openexchange.utils.ExecutorsUtil;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static java.math.BigDecimal.ROUND_HALF_EVEN;

@Component
public class RequestStatisticLogger {

    private final ExecutorService es;
    private final Map<String, RequestStatistic> statistics;

    public RequestStatisticLogger() {
        this.es = Executors.newSingleThreadExecutor();
        this.statistics = new ConcurrentHashMap<>();
    }

    public void log(final String uri, final long longevity, final long wait, final boolean success){
        es.submit(() -> {
            RequestStatistic current = statistics.getOrDefault(uri, new RequestStatistic(uri));
            current.total += 1;
            current.activeTime += longevity;
            current.waitTime += wait;
            if(success) current.success += 1;
            statistics.put(uri, current);
        });
    }

    public String getLessLoaded(){
        Optional<RequestStatistic> rs = statistics.values().stream().sorted((o1, o2) -> {
            BigDecimal aat1 = BigDecimal.valueOf(o1.activeTime).divide(BigDecimal.valueOf(o1.total), ROUND_HALF_EVEN);
            BigDecimal aat2 = BigDecimal.valueOf(o2.activeTime).divide(BigDecimal.valueOf(o2.total), ROUND_HALF_EVEN);
            return aat1.compareTo(aat2);
        }).findFirst();
        return rs.map(requestStatistic -> requestStatistic.uri).orElse(null);
    }

    public boolean learnedForAll(List<String> urisToGet){
        return statistics.keySet().containsAll(urisToGet);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("\n");
        for(RequestStatistic rs : statistics.values()) sb.append(rs.toString()).append("\n");
        return sb.toString();
    }

    @PreDestroy
    private void destroy(){
        ExecutorsUtil.shutdownExecutorService(es);
    }

    private static class RequestStatistic {
        String uri;
        long success = 0;
        long total = 0;
        long activeTime = 0;
        long waitTime = 0;

        private RequestStatistic(String uri) {
            this.uri = uri;
        }

        @Override
        public String toString() {
            return "RequestStatistic{" +
                    "uri='" + uri + '\'' +
                    ", success=" + success +
                    ", failures=" + (total - success) +
                    ", total=" + total +
                    ", average active time=" + BigDecimal.valueOf(activeTime).divide(BigDecimal.valueOf(total), ROUND_HALF_EVEN).floatValue() + " mls." +
                    ", average wait time=" + BigDecimal.valueOf(waitTime).divide(BigDecimal.valueOf(total), ROUND_HALF_EVEN).floatValue() + " mls." +
                    ", average request / sec=" + BigDecimal.valueOf(total).divide(BigDecimal.valueOf(TimeUnit.MILLISECONDS.toSeconds(waitTime)), ROUND_HALF_EVEN).floatValue() +
                    '}';
        }
    }
}

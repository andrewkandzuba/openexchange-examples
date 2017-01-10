package io.openexchange.statistics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.metrics.repository.MetricRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;


@Service
@EnableScheduling
@ConditionalOnProperty(name = "openexchange.statistic.metrics.print.enable", havingValue = "true")
public class MetricsService {
    private static final Logger logger = LoggerFactory.getLogger(MetricsService.class);
    private final MetricRepository metrics;

    @Autowired
    public MetricsService(MetricRepository metrics) {
        this.metrics = metrics;
    }

    @Scheduled(fixedDelayString = "${openexchange.statistic.metrics.print.rate:5000}", initialDelayString = "${openexchange.statistic.metrics.print.rate:5000}")
    public void export(){
        metrics.findAll().forEach(m -> logger.info("Reporting metric {}={}", m.getName(), m.getValue()));
    }
}

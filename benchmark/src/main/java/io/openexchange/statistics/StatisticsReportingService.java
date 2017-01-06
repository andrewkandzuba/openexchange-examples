package io.openexchange.statistics;

import io.openexchange.configurations.HttpLoadRunnerConfiguration;
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
public class StatisticsReportingService {
    private static final Logger logger = LoggerFactory.getLogger(StatisticsReportingService.class);
    private final StatisticsTrackingService statistics;
    private final HttpLoadRunnerConfiguration config;
    private final ScheduledExecutorService printStatisticsService;

    @Autowired
    public StatisticsReportingService(StatisticsTrackingService statistics, HttpLoadRunnerConfiguration config) {
        this.statistics = statistics;
        this.config = config;
        this.printStatisticsService = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors() + 1);
    }

    @PostConstruct
    private void init() {
        logger.debug("Starting statistics reporting service ...");
        printStatisticsService.scheduleAtFixedRate(
                () -> logger.info(statistics.toString()),
                config.getPrintRate(),
                config.getPrintRate(),
                TimeUnit.MILLISECONDS
        );
        logger.debug("Statistics reporting service has been started.");
    }

    @PreDestroy
    private void destroy() {
        logger.debug("Stopping statistics reporting service ...");
        shutdownExecutorService(printStatisticsService);
        logger.debug("Statistics reporting service has been stopped.");
    }
}

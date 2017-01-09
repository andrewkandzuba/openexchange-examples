package io.openexchange.statistics.configurations;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MetricsConfiguration {

    @Value("${openexchange.statistic.metrics.print.rate:5000}")
    private long printRate;

    public long getPrintRate() {
        return printRate;
    }
}

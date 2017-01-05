package io.openexchange.configurations;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HttpLoadRunnerConfiguration {
    @Value("${openexchange.loadrunner.hosts}")
    private String hosts;

    @Value("${openexchange.loadrunner.threads.number}")
    private int threadsNumber;

    @Value("${openexchange.loadrunner.threads.rate}")
    private long rate;

    @Value("${openexchange.loadrunner.statistic.print.rate}")
    private long printRate;

    public String getHosts() {
        return hosts;
    }

    public int getThreadsNumber() {
        return threadsNumber;
    }

    public long getRate() {
        return rate;
    }

    public long getPrintRate() {
        return printRate;
    }
}

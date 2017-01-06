package io.openexchange.configurations;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HttpLoadRunnerConfiguration {
    @Value("${openexchange.loadrunner.hosts}")
    private String hosts;

    @Value("${openexchange.loadrunner.statistic.print.rate:5000}")
    private long printRate;

    @Value("${openexchange.loadrunner.concurrency:1}")
    private int concurrency;

    @Value("${openexchange.loadrunner.rounds:1}")
    private int rounds;

    public String[] getHosts() {
        return hosts.split(",");
    }

    public long getPrintRate() {
        return printRate;
    }

    public int getConcurrency() {
        return concurrency;
    }

    public int getRounds() {
        return rounds;
    }
}

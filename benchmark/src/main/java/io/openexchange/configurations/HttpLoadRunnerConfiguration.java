package io.openexchange.configurations;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HttpLoadRunnerConfiguration {
    @Value("${openexchange.loadrunner.uris}")
    private String uris;

    @Value("${openexchange.loadrunner.concurrency:1}")
    private int concurrency;

    @Value("${openexchange.loadrunner.rounds:1}")
    private int rounds;

    public String[] getUris() {
        return uris.split(",");
    }

    public int getConcurrency() {
        return concurrency;
    }

    public int getRounds() {
        return rounds;
    }
}

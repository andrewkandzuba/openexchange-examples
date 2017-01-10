package io.openexchange.statistics;

import org.springframework.boot.actuate.metrics.repository.InMemoryMetricRepository;
import org.springframework.boot.actuate.metrics.repository.MetricRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnMissingBean(MetricRepository.class)
public class MetricsAutoConfiguration {
    @Bean
    public MetricRepository inMemoryMetricRepository() {
        return new InMemoryMetricRepository();
    }
}

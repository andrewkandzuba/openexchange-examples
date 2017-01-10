package io.openexchange.statistics;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.metrics.repository.MetricRepository;
import org.springframework.boot.actuate.metrics.writer.Delta;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = MetricsServiceTest.class)
@SpringBootApplication
@TestPropertySource(locations = "classpath:test.properties")
public class MetricsServiceTest {
    private final static String COUNTER_OPENEXCHANGE_BENCHMARK_SERVER = "counter.openexchange.benchmark.server.";
    @Autowired
    private MetricsService metricsService;
    @Autowired
    private MetricRepository metricRepository;

    @Test
    public void testMetrics() throws Exception {
        metricRepository.increment(new Delta<Number>(COUNTER_OPENEXCHANGE_BENCHMARK_SERVER + "1.total", 1));
        metricRepository.increment(new Delta<Number>(COUNTER_OPENEXCHANGE_BENCHMARK_SERVER +  "1.total", 1));
        metricRepository.increment(new Delta<Number>(COUNTER_OPENEXCHANGE_BENCHMARK_SERVER +  "1.failure",1 ));
        metricRepository.increment(new Delta<Number>(COUNTER_OPENEXCHANGE_BENCHMARK_SERVER + "1.time", 101));

        metricRepository.increment(new Delta<Number>(COUNTER_OPENEXCHANGE_BENCHMARK_SERVER + "2.total", 1));
        metricRepository.increment(new Delta<Number>( COUNTER_OPENEXCHANGE_BENCHMARK_SERVER + "2.time", 200));

        Assert.assertEquals(2L, metricRepository.findOne(COUNTER_OPENEXCHANGE_BENCHMARK_SERVER + "1.total").getValue());
        Assert.assertEquals(1L, metricRepository.findOne(COUNTER_OPENEXCHANGE_BENCHMARK_SERVER + "1.failure").getValue());
        Assert.assertEquals(101L, metricRepository.findOne(COUNTER_OPENEXCHANGE_BENCHMARK_SERVER + "1.time").getValue());

        Assert.assertEquals(1L, metricRepository.findOne(COUNTER_OPENEXCHANGE_BENCHMARK_SERVER + "2.total").getValue());
        Assert.assertNull(metricRepository.findOne(COUNTER_OPENEXCHANGE_BENCHMARK_SERVER + "2.failure"));
        Assert.assertEquals(200L, metricRepository.findOne(COUNTER_OPENEXCHANGE_BENCHMARK_SERVER + "2.time").getValue());

        metricsService.export();
    }
}

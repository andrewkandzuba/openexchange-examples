package io.openexchange.components;

import io.openexchange.configurations.HttpLoadRunnerConfiguration;
import org.apache.http.HeaderElement;
import org.apache.http.HeaderElementIterator;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.client.FutureRequestExecutionService;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.protocol.HTTP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.metrics.Metric;
import org.springframework.boot.actuate.metrics.repository.MetricRepository;
import org.springframework.boot.actuate.metrics.writer.Delta;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.net.URI;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Service
public class HttpLoadRunner {
    private final static String COUNTER_OPENEXCHANGE_BENCHMARK_SERVER = "counter.openexchange.benchmark.server";
    private final static int DEFAULT_KEEP_ALIVE = 5000;
    private final static Logger logger = LoggerFactory.getLogger(HttpLoadRunner.class);

    private final HttpLoadRunnerConfiguration config;
    private final MetricRepository metricRepository;
    private final FutureRequestExecutionService futureRequestExecutionService;
    private final ApplicationContext appContext;

    @Autowired
    public HttpLoadRunner(HttpLoadRunnerConfiguration config, MetricRepository metricRepository, ApplicationContext appContext) {
        this.config = config;
        this.metricRepository = metricRepository;
        this.futureRequestExecutionService = new FutureRequestExecutionService(create(), Executors.newFixedThreadPool(config.getConcurrency()));
        this.appContext = appContext;
    }

    @PostConstruct
    private void init() {
        logger.info("Starting load runner...");
        new Thread(() -> {
            try {
                CountDownLatch latch = new CountDownLatch(config.getUris().length * config.getRounds() * config.getConcurrency());
                logger.info("Run test");
                for (String host : config.getUris()) runRound(URI.create(host), latch);
                logger.info("Waiting for test complete");
                latch.await();
                logger.info("Test has been completed");
            } catch (InterruptedException e) {
                logger.error(e.getMessage(), e);
            } finally {
                initiateShutdown();
            }
        }).start();
        logger.info("Load runner has been started");
    }

    @PreDestroy
    private void destroy() {
        logger.info("Stopping load runner...");
        try {
            futureRequestExecutionService.close();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        logger.info("Load runner has been stopped");
    }

    private void initiateShutdown(){
        SpringApplication.exit(appContext, () -> 0);
    }

    private void runRound(URI host, CountDownLatch latch) {
        for (int r = 0; r < config.getConcurrency() * config.getRounds(); r++) {
            futureRequestExecutionService.execute(
                    new HttpGet(host),
                    HttpClientContext.create(),
                    httpResponse -> httpResponse.getStatusLine().getStatusCode() == 200,
                    new FutureCallback<Boolean>() {

                        @Override
                        public void completed(Boolean aBoolean) {
                            metricRepository.set(
                                    new Metric<Number>(
                                            COUNTER_OPENEXCHANGE_BENCHMARK_SERVER + "." + host.getHost() + ".activeConnectionCount",
                                            futureRequestExecutionService.metrics().getActiveConnectionCount()));
                            metricRepository.set(
                                    new Metric<Number>(
                                            COUNTER_OPENEXCHANGE_BENCHMARK_SERVER + "." + host.getHost() + ".taskAverageDuration",
                                            futureRequestExecutionService.metrics().getTaskAverageDuration()));
                            metricRepository.set(
                                    new Metric<Number>(
                                            COUNTER_OPENEXCHANGE_BENCHMARK_SERVER + "." + host.getHost() + ".requestAverageDuration",
                                            futureRequestExecutionService.metrics().getRequestAverageDuration()));
                            metricRepository.increment(
                                    new Delta<Number>(COUNTER_OPENEXCHANGE_BENCHMARK_SERVER + "." + host.getHost() + ".successfulRequests", 1)
                            );
                            latch.countDown();
                        }

                        @Override
                        public void failed(Exception e) {
                            metricRepository.increment(
                                    new Delta<Number>(COUNTER_OPENEXCHANGE_BENCHMARK_SERVER + "." + host.getHost() + ".failedRequests", 1)
                            );
                            latch.countDown();
                        }

                        @Override
                        public void cancelled() {
                            metricRepository.increment(
                                    new Delta<Number>(COUNTER_OPENEXCHANGE_BENCHMARK_SERVER + "." + host.getHost() + ".failedRequests", 1)
                            );
                            latch.countDown();
                        }
                    }
            );
        }
    }

    private HttpClient create(){
        return HttpClients.custom()
                .setMaxConnPerRoute(config.getConcurrency())
                .setMaxConnTotal(config.getConcurrency() * config.getUris().length)
                .evictIdleConnections(DEFAULT_KEEP_ALIVE, TimeUnit.MILLISECONDS)
                .disableRedirectHandling()
                .setKeepAliveStrategy((response, context) -> {
                    HeaderElementIterator it = new BasicHeaderElementIterator(
                            response.headerIterator(HTTP.CONN_KEEP_ALIVE));
                    while (it.hasNext()) {
                        HeaderElement he = it.nextElement();
                        String param = he.getName();
                        String value = he.getValue();
                        if (value != null && param.equalsIgnoreCase("timeout")) {
                            try {
                                return Long.parseLong(value) * 1000;
                            } catch (NumberFormatException ignore) {
                            }
                        }
                    }
                    return DEFAULT_KEEP_ALIVE;
                }).build();
    }
}

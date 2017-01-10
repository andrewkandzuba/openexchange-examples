package io.openexchange.components;

import io.openexchange.configurations.HttpLoadRunnerConfiguration;
import org.apache.http.*;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestExecutor;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.metrics.repository.MetricRepository;
import org.springframework.boot.actuate.metrics.writer.Delta;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.net.URI;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static io.openexchange.utils.ExecutorsUtil.shutdownExecutorService;

@Component
public class HttpLoadRunner {
    private static final String OPENEXCHANGE_HTTP_REQUEST_START_TIME = "openexchange.http.request.start.time";
    private final static String COUNTER_OPENEXCHANGE_BENCHMARK_SERVER = "counter.openexchange.benchmark.server";

    private static final int DEFAULT_KEEP_ALIVE = 5000;
    private static final Logger logger = LoggerFactory.getLogger(HttpLoadRunner.class);

    private final HttpLoadRunnerConfiguration config;
    private final ExecutorService hostsExecutorService;
    private final CloseableHttpClient httpClient;
    private final MetricRepository metricRepository;

    @Autowired
    public HttpLoadRunner(HttpLoadRunnerConfiguration config, PoolingHttpClientConnectionManager cm, MetricRepository metricRepository) {
        this.config = config;
        this.hostsExecutorService = Executors.newFixedThreadPool(config.getUris().length);
        this.httpClient = HttpClients.custom()
                .setConnectionManager(cm)
                .disableRedirectHandling()
                .setRequestExecutor(new HttpRequestExecutor() {

                    @Override
                    protected HttpResponse doSendRequest(
                            final HttpRequest request,
                            final HttpClientConnection conn,
                            final HttpContext context) throws IOException, HttpException {
                        context.setAttribute(OPENEXCHANGE_HTTP_REQUEST_START_TIME, System.currentTimeMillis());
                        HttpResponse response = super.doSendRequest(request, conn, context);
                        HttpConnectionMetrics metrics = conn.getMetrics();
                        metrics.reset();
                        return response;
                    }

                    @Override
                    protected HttpResponse doReceiveResponse(
                            final HttpRequest request,
                            final HttpClientConnection conn,
                            final HttpContext context) throws HttpException, IOException {
                        HttpResponse response = super.doReceiveResponse(request, conn, context);
                        long total = System.currentTimeMillis() - (Long) (context.getAttribute(OPENEXCHANGE_HTTP_REQUEST_START_TIME));
                        String host = URI.create(context.getAttribute("http.target_host").toString()).getHost();
                        metricRepository.increment(
                                new Delta<Number>(
                                        COUNTER_OPENEXCHANGE_BENCHMARK_SERVER + "." + host + ".time",
                                        total));
                        HttpConnectionMetrics metrics = conn.getMetrics();
                        metrics.reset();
                        return response;
                    }

                }).setKeepAliveStrategy((response, context) -> {
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
        this.metricRepository = metricRepository;
    }

    @PostConstruct
    private void init() {
        logger.info("Starting load runner...");
        for (String host : config.getUris())
            hostsExecutorService.submit(() -> runRound(host, config.getRounds(), config.getConcurrency()));
        logger.info("Load runner has been started");
    }

    @PreDestroy
    private void destroy() {
        logger.info("Stopping load runner...");
        shutdownExecutorService(hostsExecutorService);
        try {
            httpClient.close();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        logger.info("Load runner has been stopped");
    }

    private void runRound(String host, int rounds, int concurrency) {
        for (int r = 0; r < rounds; r++) {
            Thread[] threads = new Thread[concurrency];
            for (int c = 0; c < threads.length; c++) {
                threads[c] = new GetThread(new HttpGet(host));
            }
            for (Thread thread : threads) {
                thread.start();
            }
            for (Thread thread : threads) {
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }
    }

    private void query(final HttpGet httpGet, final HttpContext httpContext) {
        String host = httpGet.getURI().getHost();
        try (CloseableHttpResponse response = httpClient.execute(httpGet, httpContext)) {
            if (response.getStatusLine().getStatusCode() == 200) {
                EntityUtils.consume(response.getEntity());
                metricRepository.increment(
                        new Delta<Number>(COUNTER_OPENEXCHANGE_BENCHMARK_SERVER + "." + host + ".success", 1)
                );
            }
        } catch (IOException e) {
            metricRepository.increment(
                    new Delta<Number>(COUNTER_OPENEXCHANGE_BENCHMARK_SERVER + "." + host + ".failure", 1)
            );
            logger.error(e.getMessage(), e);
        }
    }

    private class GetThread extends Thread {
        private final HttpGet httpget;
        private final HttpContext httpContext;

        private GetThread(HttpGet httpget) {
            this.httpget = httpget;
            this.httpContext = HttpClientContext.create();
        }

        @Override
        public void run() {
            query(httpget, httpContext);
        }
    }
}

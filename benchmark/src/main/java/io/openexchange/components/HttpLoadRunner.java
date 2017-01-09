package io.openexchange.components;

import io.openexchange.configurations.HttpLoadRunnerConfiguration;
import io.openexchange.statistics.Tracking;
import io.openexchange.statistics.metrics.Request;
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
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static io.openexchange.utils.ExecutorsUtil.shutdownExecutorService;

@Component
public class HttpLoadRunner {
    private static final Logger logger = LoggerFactory.getLogger(HttpLoadRunner.class);

    private final HttpLoadRunnerConfiguration config;
    private final ExecutorService hostsExecutorService;
    private final CloseableHttpClient httpClient;
    private final Tracking statistics;

    @Autowired
    public HttpLoadRunner(HttpLoadRunnerConfiguration config, PoolingHttpClientConnectionManager cm, Tracking statistics) {
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
                        context.setAttribute("openexchange.http.request.start.time", System.currentTimeMillis());
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
                        long total = System.currentTimeMillis() - (Long) (context.getAttribute("openexchange.http.request.start.time"));
                        context.setAttribute("openexchange.http.request.total.time", total);
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
                    return 30000;
                }).build();
        this.statistics = statistics;
    }

    @PostConstruct
    private void init() {
        logger.info("Starting load runner...");
        for (String host : config.getUris())
            hostsExecutorService.submit(() -> runRound(host, config.getRounds()));
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

    private void runRound(String host, int rounds) {
        for (int r = 0; r < rounds; r++) {
            Thread[] threads = new Thread[config.getConcurrency()];
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

    private void query(final HttpGet httpget, final HttpContext httpContext) {
        boolean success = false;
        try (CloseableHttpResponse response = httpClient.execute(httpget, httpContext)) {
            if (response.getStatusLine().getStatusCode() == 200) {
                EntityUtils.consume(response.getEntity());
                success = true;
            }
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        statistics.log(InetSocketAddress.createUnresolved(httpget.getURI().getHost(), httpget.getURI().getPort()),
                new Request(success, (Long) httpContext.getAttribute("openexchange.http.request.total.time")));
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

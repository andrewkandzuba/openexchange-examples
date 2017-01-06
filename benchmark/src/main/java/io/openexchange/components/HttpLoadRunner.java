package io.openexchange.components;

import io.openexchange.configurations.HttpLoadRunnerConfiguration;
import io.openexchange.statistics.StatisticsTrackingService;
import org.apache.http.HeaderElement;
import org.apache.http.HeaderElementIterator;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static io.openexchange.utils.ExecutorsUtil.shutdownExecutorService;

@Component
public class HttpLoadRunner {
    private static final Logger logger = LoggerFactory.getLogger(HttpLoadRunner.class);

    private final HttpLoadRunnerConfiguration config;
    private final StatisticsTrackingService statistics;

    private final ExecutorService requestExecutorService;
    private final CloseableHttpClient httpClient;

    @Autowired
    public HttpLoadRunner(HttpLoadRunnerConfiguration config, PoolingHttpClientConnectionManager cm, StatisticsTrackingService statistics) {
        this.config = config;
        this.requestExecutorService = Executors.newWorkStealingPool();
        this.httpClient = HttpClients.custom()
                .setConnectionManager(cm)
                .disableRedirectHandling()
                .setKeepAliveStrategy((response, context) -> {
                    // Honor 'keep-alive' header
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
        runRound();
        logger.info("Load runner has been started");
    }

    @PreDestroy
    private void destroy() {
        logger.info("Stopping load runner...");
        shutdownExecutorService(requestExecutorService);
        try {
            httpClient.close();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        logger.info("Load runner has been stopped");
    }

    private void runRound(){
        for (String host : config.getHosts()) {
            for (int c = 0; c < config.getConcurrency(); c++) {
                requestExecutorService.execute(new GetUriThread(httpClient, new HttpGet(host)));
            }
        }
    }

    private class GetUriThread extends Thread {
        private final CloseableHttpClient httpClient;
        private final HttpGet httpget;
        private final HttpContext httpContext;

        private GetUriThread(CloseableHttpClient httpClient, HttpGet httpget) {
            this.httpClient = httpClient;
            this.httpget = httpget;
            this.httpContext = HttpClientContext.create();
        }

        @Override
        public void run() {
            logger.debug("HTTP GET " + httpget.getURI());
            boolean success = false;
            long requestTime = 0;
            long loopStartTime = System.currentTimeMillis();
            long requestStartTime = System.currentTimeMillis();
            try (CloseableHttpResponse response = httpClient.execute(httpget, httpContext)) {
                requestTime = System.currentTimeMillis() - requestStartTime;
                if (response.getStatusLine().getStatusCode() == 200) {
                    HttpEntity entity = response.getEntity();
                    EntityUtils.consume(entity);
                    success = true;
                }
            } catch (IOException e) {
                logger.debug(e.getMessage(), e);
            }
            statistics.log(httpget.getURI(), requestTime, System.currentTimeMillis() - loopStartTime, success);
        }
    }
}

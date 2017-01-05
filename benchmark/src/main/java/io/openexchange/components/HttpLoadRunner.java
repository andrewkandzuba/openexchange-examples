package io.openexchange.components;

import io.openexchange.configurations.HttpLoadRunnerConfiguration;
import io.openexchange.statistics.RequestStatisticLogger;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;

import static io.openexchange.utils.ExecutorsUtil.shutdownExecutorService;

@Component
public class HttpLoadRunner {
    private static final Logger logger = LoggerFactory.getLogger(HttpLoadRunner.class);

    private final PrintStatisticsMonitorThread printMonitor;
    private final ExecutorService requestExecutorService;
    private final PoolingHttpClientConnectionManager cm;
    private final RequestStatisticLogger requestStatisticLogger;
    private final HttpLoadRunnerConfiguration appConf;

    @Autowired
    public HttpLoadRunner(HttpLoadRunnerConfiguration appConf, PoolingHttpClientConnectionManager cm, RequestStatisticLogger requestStatisticLogger) {
        this.appConf = appConf;
        this.printMonitor = new PrintStatisticsMonitorThread();
        this.requestExecutorService = Executors.newCachedThreadPool();
        this.cm = cm;
        this.requestStatisticLogger = requestStatisticLogger;
    }

    @PostConstruct
    private void init() {
        logger.info("Starting load runner...");
        printMonitor.start();
        for (int i = 0; i < appConf.getThreadsNumber(); i++)
            requestExecutorService.execute(new GetThread(cm, appConf.getHosts(), appConf.getRate()));
        logger.info("Load runner has been started");
    }

    @PreDestroy
    private void destroy() {
        logger.info("Stopping load runner...");
        shutdownExecutorService(requestExecutorService);
        printMonitor.shutdown();
        logger.info("Load runner has been stopped");
    }

    private class PrintStatisticsMonitorThread extends Thread {
        private volatile boolean shutdown;

        @Override
        public void run() {
            logger.info("Enters print statistics idleMonitor");
            try {
                while (!shutdown) {
                    synchronized (this) {
                        wait(appConf.getPrintRate());
                        logger.info(requestStatisticLogger.toString());
                    }
                }
            } catch (InterruptedException e) {
                logger.error(e.getMessage(), e);
            }
            logger.info("Exits print statistics connection idleMonitor");
        }

        void shutdown() {
            shutdown = true;
            synchronized (this) {
                notifyAll();
            }
        }
    }

    private class GetThread extends Thread {
        private final HttpClientConnectionManager cm;
        private final List<String> urisToGet;
        private final long delay;

        GetThread(HttpClientConnectionManager cm, String urisToGet, long delay) {
            this.cm = cm;
            this.urisToGet = Arrays.asList(urisToGet.split(","));
            this.delay = delay;
        }

        @Override
        public void run() {
            try (CloseableHttpClient httpClient = HttpClients.custom().setConnectionManager(cm).disableRedirectHandling().build()) {
                while (!Thread.currentThread().isInterrupted() && delay > 0) {
                    long waitBeforeNext = ThreadLocalRandom.current().nextLong(delay);
                    Thread.sleep(waitBeforeNext);

                    Collections.shuffle(urisToGet);
                    for (String uriToGet : urisToGet) {

                        logger.debug("HTTP GET: " + uriToGet);

                        HttpContext context = HttpClientContext.create();
                        HttpGet httpget = new HttpGet(uriToGet);
                        long startRequest = System.currentTimeMillis();

                        try (CloseableHttpResponse response = httpClient.execute(httpget, context)) {
                            int statusCode = response.getStatusLine().getStatusCode();

                            if (statusCode == 200) {
                                HttpEntity entity = response.getEntity();
                                logger.debug(EntityUtils.toString(entity));
                                requestStatisticLogger.log(uriToGet, System.currentTimeMillis() - startRequest, waitBeforeNext, true);
                                logger.debug("HTTP GET Response code: " + statusCode + " for: " + uriToGet);
                                break;
                            }
                            requestStatisticLogger.log(uriToGet, System.currentTimeMillis() - startRequest, waitBeforeNext, false);

                        } catch (IOException e) {
                            logger.debug(e.getMessage(), e);
                            requestStatisticLogger.log(uriToGet, System.currentTimeMillis() - startRequest, waitBeforeNext, false);
                        }
                    }
                }
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            } catch (InterruptedException e) {
                // Just exist
            }
        }
    }
}

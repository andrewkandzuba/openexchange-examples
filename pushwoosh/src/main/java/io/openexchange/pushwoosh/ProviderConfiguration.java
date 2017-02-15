package io.openexchange.pushwoosh;

import io.openexchange.domain.Application;
import io.openexchange.domain.Device;
import io.openexchange.domain.User;
import io.openexchange.http.HttpClientUtils;
import io.openexchange.services.Registry;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.client.FutureRequestExecutionService;
import org.apache.http.impl.client.HttpRequestFutureTask;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executors;

@Configuration
@RefreshScope
public class ProviderConfiguration {
    @Value("${openexchange.httpclient.connectionmanager.defaultmaxperroute:50}")
    private int defaultMaxPerRoute;
    @Value("${openexchange.httpclient.connectionmanager.maxtotal:200}")
    private int maxTotal;

    @Value("${openexchange.pushwoosh.api.accesstoken}")
    private String accessToken;

    @Value("${openexchange.pushwoosh.api.endpoint}")
    private String endpoint;

    @Bean
    public PoolingHttpClientConnectionManager httpClientConnectionManager() {
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        cm.setMaxTotal(maxTotal);
        cm.setDefaultMaxPerRoute(defaultMaxPerRoute);
        return cm;
    }

    @Bean
    public Registry registry() {
        return new Registry() {
            private final FutureRequestExecutionService fes =
                    new FutureRequestExecutionService(
                            HttpClientUtils.create(defaultMaxPerRoute, maxTotal),
                            Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() + 1));

            @Override
            public boolean assign(User user, Application application, Device device) {
                HttpPost httpPost = new HttpPost(endpoint + "/registerUser");
                //String json = "{"id":1,"name":"John"}";
                //StringEntity entity = new StringEntity(json);
                //httpPost.setEntity(entity);
                httpPost.setHeader("Accept", "application/json");
                httpPost.setHeader("Content-type", "application/json");
                HttpRequestFutureTask<Boolean> futureTask = fes.execute(
                        httpPost,
                        HttpClientContext.create(),
                        httpResponse -> httpResponse.getStatusLine().getStatusCode() == 200,
                        new FutureCallback<Boolean>() {
                            @Override
                            public void completed(Boolean aBoolean) {
                            }

                            @Override
                            public void failed(Exception e) {
                            }

                            @Override
                            public void cancelled() {
                            }
                        }
                );
                return futureTask.isDone();
            }
        };
    }
}

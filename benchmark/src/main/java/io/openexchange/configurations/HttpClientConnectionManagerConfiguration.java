package io.openexchange.configurations;

import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HttpClientConnectionManagerConfiguration {
    @Value("${openexchange.httpclient.connectionmanager.maxtotal:200}")
    private int maxTotal;

    @Value("${openexchange.httpclient.connectionmanager.defaultmaxperroute:50}")
    private int defaultMaxPerRoute;

    @Bean
    public PoolingHttpClientConnectionManager httpClientConnectionManager(){
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        cm.setMaxTotal(maxTotal);
        cm.setDefaultMaxPerRoute(defaultMaxPerRoute);
        return cm;
    }
}

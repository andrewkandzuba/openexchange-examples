package io.openexchange.http;

import org.apache.http.HeaderElement;
import org.apache.http.HeaderElementIterator;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.protocol.HTTP;

import java.util.concurrent.TimeUnit;

public abstract class HttpClientUtils {
    private final static int DEFAULT_KEEP_ALIVE = 5000;

    public static HttpClient create(int maxConnPerRoute, int maxConnTotal) {
        return HttpClients.custom()
                .setMaxConnPerRoute(maxConnPerRoute)
                .setMaxConnTotal(maxConnTotal)
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

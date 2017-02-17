package io.openexchange.pushwoosh;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.openexchange.pojos.domain.Application;
import io.openexchange.pojos.domain.Device;
import io.openexchange.pojos.domain.User;
import io.openexchange.pojos.pushwoosh.*;
import io.openexchange.services.Registry;
import io.openexchange.services.Reporter;
import io.openexchange.services.Sender;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Configuration
public class ProviderConfiguration {
    private static final Logger logger = LoggerFactory.getLogger(ProviderConfiguration.class);

    @Value("${openexchange.httpclient.connectionmanager.defaultmaxperroute:50}")
    private int defaultMaxPerRoute;
    @Value("${openexchange.httpclient.connectionmanager.maxtotal:200}")
    private int maxTotal;
    @Value("${openexchange.pushwoosh.api.accesstoken}")
    private String accessToken;
    @Value("${openexchange.pushwoosh.api.endpoint:https://cp.pushwoosh.com/json/1.3}")
    private String endpoint;

    @Bean
    public PoolingHttpClientConnectionManager httpClientConnectionManager() {
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        cm.setMaxTotal(maxTotal);
        cm.setDefaultMaxPerRoute(defaultMaxPerRoute);
        return cm;
    }

    @Bean
    @ConditionalOnMissingBean(Registry.class)
    public Registry registry() {
        return new Registry() {
            @Override
            public boolean assign(User user, Application application, Device device) throws IOException, PushWooshResponseException {
                return execute("registerUser",
                        new PushRequest()
                                .withRequest(
                                        new RegisterUser()
                                                .withAuth(accessToken)
                                                .withApplication(application.getCode())
                                                .withHwid(device.getHwid())
                                                .withUserId(user.getId()))).getStatusCode() == 200;
            }

            @Override
            public boolean add(Application application, Device device) throws IOException, PushWooshResponseException {
                return execute("registerDevice",
                        new PushRequest()
                                .withRequest(
                                        new RegisterDevice()
                                                .withAuth(accessToken)
                                                .withApplication(application.getCode())
                                                .withHwid(device.getHwid())
                                                .withPushToken(device.getToken())
                                                .withDeviceType(device.getType().value()))).getStatusCode() == 200;
            }

            @Override
            public boolean remove(Application application, Device device) throws IOException, PushWooshResponseException {
                return execute("unregisterDevice",
                        new PushRequest()
                                .withRequest(
                                        new RegisterDevice()
                                                .withAuth(accessToken)
                                                .withApplication(application.getCode())
                                                .withHwid(device.getHwid())
                                                .withPushToken(device.getToken())
                                                .withDeviceType(device.getType().value()))).getStatusCode() == 200;
            }
        };
    }

    @Bean
    @ConditionalOnMissingBean(Sender.class)
    public Sender sender() {
        return new Sender() {
            @Override
            public List<String> push(Application application, String text, User... users) throws IOException, PushWooshResponseException {
                return Collections.unmodifiableList(
                        execute("createMessage",
                                new PushRequest()
                                        .withRequest(
                                                new CreateMessage()
                                                        .withAuth(accessToken)
                                                        .withApplication(application.getCode())
                                                        .withNotifications(
                                                                Collections.singletonList(
                                                                        new Notification()
                                                                                .withContent(text)
                                                                                .withUsers(Stream.of(users).map(User::getId).collect(Collectors.toList()))))))
                                .getResponse().getMessages());
            }

            @Override
            public List<String> push(Application application, String text, Device... devices) throws IOException, PushWooshResponseException {
                return Collections.unmodifiableList(
                        execute("createMessage",
                                new PushRequest()
                                        .withRequest(
                                                new CreateMessage()
                                                        .withAuth(accessToken)
                                                        .withApplication(application.getCode())
                                                        .withNotifications(
                                                                Collections.singletonList(
                                                                        new Notification()
                                                                                .withContent(text)
                                                                                .withDevices(Stream.of(devices).map(Device::getHwid).collect(Collectors.toList()))))))
                                .getResponse().getMessages());
            }
        };
    }

    @Bean
    @ConditionalOnMissingBean(Reporter.class)
    public Reporter reporter() {
        return new Reporter() {
            @Override
            public String getMessageStats(String messageId) throws IOException, PushWooshResponseException {
                return execute("getMsgStats",
                        new PushRequest()
                                .withRequest(
                                        new GetMsgStats()
                                                .withAuth(accessToken)
                                                .withMessage(messageId))).getResponse().getRequestId();
            }

            @Override
            public List<Row> getResults(String requestId) throws IOException, PushWooshResponseException {
                return Collections.unmodifiableList(
                        execute("getResults",
                                new PushRequest()
                                        .withRequest(
                                                new GetResults()
                                                        .withAuth(accessToken)
                                                        .withRequestId(requestId)))
                                .getResponse().getRows());
            }
        };
    }

    private PushResponse execute(String method, PushRequest pushRequest) throws IOException, PushWooshResponseException {
        String payload = new ObjectMapper().writeValueAsString(pushRequest);
        logger.debug(String.format("execute: [method=%s, payload=%s]", method, payload));
        return handleResponse(Executor.newInstance(HttpClientBuilder.create().useSystemProperties().build())
                .execute(Request
                        .Post(endpoint + "/" + method)
                        .body(new StringEntity(payload))
                        .setHeader("Content-type", "application/json")
                        .connectTimeout(1000))
                .returnResponse());
    }

    private PushResponse handleResponse(final HttpResponse response) throws IOException, PushWooshResponseException {
        StatusLine statusLine = response.getStatusLine();
        HttpEntity entity = response.getEntity();
        if (statusLine.getStatusCode() >= 300) {
            throw new HttpResponseException(
                    statusLine.getStatusCode(),
                    statusLine.getReasonPhrase());
        }
        if (entity == null) {
            throw new ClientProtocolException("Response contains no content");
        }

        String payload = EntityUtils.toString(entity);
        logger.debug(String.format("handleResponse: [payload=%s]", payload));
        PushResponse pushResponse = new ObjectMapper().readValue(payload, PushResponse.class);
        if (pushResponse.getStatusCode() != 200) {
            throw new PushWooshResponseException(
                    pushResponse.getStatusCode(),
                    pushResponse.getStatusMessage());
        }
        return pushResponse;
    }
}

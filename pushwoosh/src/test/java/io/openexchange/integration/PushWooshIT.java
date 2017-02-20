package io.openexchange.integration;

import io.openexchange.pojos.domain.Application;
import io.openexchange.pojos.domain.Device;
import io.openexchange.pojos.domain.User;
import io.openexchange.pojos.pushwoosh.Row;
import io.openexchange.pushwoosh.ProviderConfiguration;
import io.openexchange.pushwoosh.PushWooshResponseException;
import io.openexchange.services.Registry;
import io.openexchange.services.Reporter;
import io.openexchange.services.Sender;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {
        ProviderConfiguration.class
})
public class PushWooshIT {
    @Autowired
    private Registry registry;
    @Autowired
    private Sender sender;
    @Autowired
    private Reporter reporter;

    private User user;
    private Application app;
    private Device device;

    @Value("${openexchange.pushwoosh.test.userid}")
    private String userId;
    @Value("${openexchange.pushwoosh.test.applicationcode}")
    private String applicationCode;
    @Value("${openexchange.pushwoosh.test.devicehwid}")
    private String deviceHwId;
    @Value("${openexchange.pushwoosh.test.devicepushtoken}")
    private String devicePushToken;
    @Value("${openexchange.pushwoosh.test.devicetype:1}")
    private String deviceType;

    @Before
    public void setUp() throws Exception {
        this.user = new User().withId(userId);
        this.app = new Application().withCode(applicationCode);
        this.device = new Device()
                .withHwid(deviceHwId)
                .withToken(devicePushToken)
                .withType(Device.Type.fromValue(Integer.valueOf(deviceType)));
        assertTrue(registry.add(app, device));
    }

    /**
     * Requires Enterprise PushWoosh account to operate without PushWooshResponseException.
     * See: http://docs.pushwoosh.com/docs/user-id-push for more details.
     *
     * @throws IOException                - when communication channel is broken or HTTP response contains any code >= 200.
     * @throws PushWooshResponseException - if logical PushWoosh exception has happened.
     */
    @Test(expected = PushWooshResponseException.class)
    public void registerUserAndSendNotification() throws IOException, PushWooshResponseException {
        assertTrue(registry.assign(user, app, device));
        assertNotNull(sender.push(app, "Push to user", user));
    }

    @Test
    public void sendToDevice() throws IOException, PushWooshResponseException {
        assertNotNull(sender.push(app, "Push to device", device));
    }

    @Test
    public void trackMessageStatistics() throws IOException, PushWooshResponseException, InterruptedException {
        List<String> messages = sender.push(app, "Push to device with tracking", device);
        assertTrue(messages.size() == 1);
        String requestId = reporter.getMessageStats(messages.get(0));
        assertNotNull(requestId);
        List<Row> rows = reporter.getResults(requestId);
        assertNotNull(rows);
    }

    @After
    public void tearDown() throws IOException, PushWooshResponseException {
        assertTrue(registry.remove(app, device));
    }
}

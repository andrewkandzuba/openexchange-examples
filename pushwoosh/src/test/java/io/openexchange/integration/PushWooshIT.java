package io.openexchange.integration;

import io.openexchange.pojos.domain.Application;
import io.openexchange.pojos.domain.Device;
import io.openexchange.pojos.domain.User;
import io.openexchange.pushwoosh.ProviderConfiguration;
import io.openexchange.pushwoosh.PushWooshResponseException;
import io.openexchange.services.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

import static org.junit.Assert.*;

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

        Reply reply = registry.add(app, device);
        assertEquals(200, reply.getCode());
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
        Reply reply = registry.assign(user, app, device);
        assertEquals(200, reply.getCode());

        PushReply pushReply = sender.push(app, "Push to user", user);
        assertEquals(200, pushReply.getCode());
        assertNotNull(pushReply.getMessageId());
    }

    @Test
    public void sendToDevice() throws IOException, PushWooshResponseException {
        PushReply pushReply = sender.push(app, "Push to device", device);
        assertEquals(pushReply.getCode(), 200);
        assertNotNull(pushReply.getMessageId());
    }

    @Test
    public void trackMessageStatistics() throws IOException, PushWooshResponseException, InterruptedException {
        PushReply pushReply = sender.push(app, "Push to device with tracking", device);
        assertEquals(pushReply.getCode(), 200);
        assertNotNull(pushReply.getMessageId());

        RequestReply requestReply = reporter.getMessageStats(pushReply.getMessageId());
        assertEquals(requestReply.getCode(), 200);
        assertNotNull(requestReply.getRequestId());

        try {
            reporter.getResults(requestReply.getRequestId());
        } catch (PushWooshResponseException ex) {
            assertEquals(ex.getCode(), 420);
        }
    }

    @After
    public void tearDown() throws IOException, PushWooshResponseException {
        Reply reply = registry.remove(app, device);
        assertEquals(200, reply.getCode());
    }
}

package io.openexchange.integration;

import io.openexchange.domain.Application;
import io.openexchange.domain.Device;
import io.openexchange.domain.Type;
import io.openexchange.domain.User;
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

    private final User user = new User("push.user@pushwoosh.com");
    private final Application app = new Application("93689-F08F3");
    private final Device device = new Device(
            "5E9C794E-BF59-4DC9-A501-4CB56150CA43",
            "e8f22461ff9518e70def5b7203f6f91a0125837ff59edb90c95905e678a0501e",
            Type.IOS);

    @Before
    public void setUp() throws Exception {
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

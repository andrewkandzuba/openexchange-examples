package io.openexchange.integration;

import io.openexchange.domain.Application;
import io.openexchange.domain.Device;
import io.openexchange.domain.Type;
import io.openexchange.domain.User;
import io.openexchange.pushwoosh.ProviderConfiguration;
import io.openexchange.services.Registry;
import io.openexchange.services.RegistryTest;
import io.openexchange.services.Sender;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {
        RegistryTest.class,
        ProviderConfiguration.class
})
@TestPropertySource("classpath:test.properties")
public class PushWooshIT {
    @Autowired
    private Registry registry;
    @Autowired
    private Sender sender;

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

    @Test
    public void registerUserAndSendNotification() throws Exception {
        assertTrue(registry.assign(user, app, device));
        assertTrue(sender.push(app, "Push to user", user));
    }

    @Test
    public void sendToDevice() throws Exception {
        assertTrue(sender.push(app, "Push to device", device));
    }

    @After
    public void tearDown() throws Exception {
        assertTrue(registry.remove(app, device));
    }
}

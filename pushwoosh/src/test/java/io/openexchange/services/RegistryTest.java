package io.openexchange.services;

import io.openexchange.domain.Application;
import io.openexchange.domain.Device;
import io.openexchange.domain.Type;
import io.openexchange.domain.User;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = RegistryTest.class)
@TestPropertySource("classpath:test.properties")
public class RegistryTest {
    @MockBean
    private Registry registry;
    @MockBean
    private Sender sender;

    private final User user = new User("push.user@pushwoosh.com");
    private final Application app = new Application("E912E-5E034");
    private final Device device = new Device(
            "5E9C794E-BF59-4DC9-A501-4CB56150CA43",
            "e8f22461ff9518e70def5b7203f6f91a0125837ff59edb90c95905e678a0501e",
            Type.IOS);

    @Before
    public void setUp() throws Exception {
        initMocks(this);
    }

    @Test
    public void registerApplication() throws Exception {
        when(registry.assign(any(User.class), any(Application.class), any(Device.class))).thenReturn(true);
        Assert.assertTrue(registry.assign(user, app, device));
    }

    @Test(expected = IllegalStateException.class)
    public void sendApplicationNotRegistered() throws Exception {
        when(sender.push(any(Application.class), any(String.class), any(User.class)))
                .thenThrow(new IllegalStateException("Application is not registered"));
        sender.push(app, "hello!", user);
    }
}

package io.openexchange.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.openexchange.controlllers.RegistryController;
import io.openexchange.pojos.api.RegisterDevicePayload;
import io.openexchange.pojos.api.RegistryUserPayload;
import io.openexchange.pojos.domain.Application;
import io.openexchange.pojos.domain.Device;
import io.openexchange.pojos.domain.User;
import io.openexchange.services.Registry;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource("classpath:test.properties")
public class RegisterControllerTest {
    @MockBean
    private Registry registry;
    @InjectMocks
    private RegistryController registryController;
    private MockMvc mockMvc;
    private final ObjectMapper mapper = new ObjectMapper();

    private final Application application = new Application().withCode("2222-3333");
    private final Device device = new Device().withHwid("xxxxxxx").withToken("yyyyyy").withType(Device.Type._1);
    private final User user = new User().withId("zzzzzz");

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        this.mockMvc = standaloneSetup(registryController).build();
    }

    @Test
    public void registryValidationFailed() throws Exception {
        mockMvc.perform(post("/registry/add")
                .content(mapper.writeValueAsString(new RegisterDevicePayload()))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/registry/remove")
                .content(mapper.writeValueAsString(new RegisterDevicePayload()))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void registryValidationSuccess() throws Exception {
        when(registry.add(any(Application.class), any(Device.class))).thenReturn(true);
        when(registry.remove(any(Application.class), any(Device.class))).thenReturn(true);

        mockMvc.perform(post("/registry/add")
                .content(
                        mapper.writeValueAsString(
                                new RegisterDevicePayload()
                                        .withApplication(application)
                                        .withDevice(device)))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        mockMvc.perform(post("/registry/remove")
                .content(
                        mapper.writeValueAsString(
                                new RegisterDevicePayload()
                                        .withApplication(application)
                                        .withDevice(device)))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void registrationInternalError() throws Exception {
        when(registry.add(any(Application.class), any(Device.class))).thenReturn(false);
        when(registry.remove(any(Application.class), any(Device.class))).thenReturn(false);

        mockMvc.perform(post("/registry/add")
                .content(
                        mapper.writeValueAsString(
                                new RegisterDevicePayload()
                                        .withApplication(application)
                                        .withDevice(device)))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());

        mockMvc.perform(post("/registry/remove")
                .content(
                        mapper.writeValueAsString(
                                new RegisterDevicePayload()
                                        .withApplication(application)
                                        .withDevice(device)))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    public void assignUserToApplication() throws Exception {
        when(registry.assign(any(User.class), any(Application.class), any(Device.class))).thenReturn(true);

        mockMvc.perform(post("/registry/assign")
                .content(
                        mapper.writeValueAsString(
                                new RegistryUserPayload()
                                        .withApplication(application)
                                        .withDevice(device)
                                        .withUser(user)))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}

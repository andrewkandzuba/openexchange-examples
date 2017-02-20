package io.openexchange.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.openexchange.api.Utils;
import io.openexchange.controlllers.RegistryController;
import io.openexchange.pojos.api.*;
import io.openexchange.pojos.domain.Application;
import io.openexchange.pojos.domain.Device;
import io.openexchange.pojos.domain.User;
import io.openexchange.pushwoosh.PushWooshResponseException;
import io.openexchange.services.Registry;
import io.openexchange.services.Reply;
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

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
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
    public void validationFailed() throws Exception {
        mockMvc.perform(post("/registry/add")
                .content(mapper.writeValueAsString(new RegisterDeviceRequest()))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(mvcResult -> {
                    ValidationErrorResponse r = Utils.deserializeFrom(
                            mvcResult
                                    .getResponse()
                                    .getContentAsString(),
                            ValidationErrorResponse.class);
                    assertEquals(201, r.getCode().intValue());
                    assertTrue(r.getDescription().startsWith("Validation failed. "));
                    assertTrue(r.getErrors().size() > 0);
                });

        mockMvc.perform(post("/registry/remove")
                .content(mapper.writeValueAsString(new RegisterDeviceRequest()))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(mvcResult -> {
                    ValidationErrorResponse r = Utils.deserializeFrom(
                            mvcResult
                                    .getResponse()
                                    .getContentAsString(),
                            ValidationErrorResponse.class);
                    assertEquals(201, r.getCode().intValue());
                    assertTrue(r.getDescription().startsWith("Validation failed. "));
                    assertTrue(r.getErrors().size() > 0);
                });

        mockMvc.perform(post("/registry/assign")
                .content(mapper.writeValueAsString(new RegisterUserRequest()))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(mvcResult -> {
                    ValidationErrorResponse r = Utils.deserializeFrom(
                            mvcResult
                                    .getResponse()
                                    .getContentAsString(),
                            ValidationErrorResponse.class);
                    assertEquals(201, r.getCode().intValue());
                    assertTrue(r.getDescription().startsWith("Validation failed. "));
                    assertTrue(r.getErrors().size() > 0);
                });
    }

    @Test
    public void internalError() throws Exception {
        final String internalErrorMessage = "Connection timeout";

        when(registry.add(any(Application.class), any(Device.class)))
                .thenThrow(new IOException(internalErrorMessage));
        when(registry.remove(any(Application.class), any(Device.class)))
                .thenThrow(new IOException(internalErrorMessage));
        when(registry.assign(any(User.class), any(Application.class), any(Device.class)))
                .thenThrow(new IOException(internalErrorMessage));

        mockMvc.perform(post("/registry/add")
                .content(
                        mapper.writeValueAsString(
                                new RegisterDeviceRequest()
                                        .withApplication(application)
                                        .withDevice(device)))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(mvcResult -> {
                    Response r = Utils.deserializeFrom(mvcResult.getResponse().getContentAsString(), Response.class);
                    assertEquals(201, r.getCode().intValue());
                    assertEquals(internalErrorMessage, r.getDescription());
                });

        mockMvc.perform(post("/registry/remove")
                .content(
                        mapper.writeValueAsString(
                                new RegisterDeviceRequest()
                                        .withApplication(application)
                                        .withDevice(device)))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(mvcResult -> {
                    Response r = Utils.deserializeFrom(mvcResult.getResponse().getContentAsString(), Response.class);
                    assertEquals(201, r.getCode().intValue());
                    assertEquals(internalErrorMessage, r.getDescription());
                });

        mockMvc.perform(post("/registry/assign")
                .content(
                        mapper.writeValueAsString(
                                new RegisterUserRequest()
                                        .withApplication(application)
                                        .withDevice(device)
                                        .withUser(user)))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(mvcResult -> {
                    Response r = Utils.deserializeFrom(mvcResult.getResponse().getContentAsString(), Response.class);
                    assertEquals(201, r.getCode().intValue());
                    assertEquals(internalErrorMessage, r.getDescription());
                });
    }

    @Test
    public void pushWooshLogicFailed() throws Exception {
        final String errorMessageAdd = "Unable to register device.";
        final String errorMessageRemove = "Unable to remove device.";
        final String errorMessageAssign = "Unable to assign user.";

        when(registry.add(any(Application.class), any(Device.class)))
                .thenThrow(new PushWooshResponseException(201, errorMessageAdd));
        when(registry.remove(any(Application.class), any(Device.class)))
                .thenThrow(new PushWooshResponseException(201, errorMessageRemove));
        when(registry.assign(any(User.class), any(Application.class), any(Device.class)))
                .thenThrow(new PushWooshResponseException(201, errorMessageAssign));

        mockMvc.perform(post("/registry/add")
                .content(
                        mapper.writeValueAsString(
                                new RegisterDeviceRequest()
                                        .withApplication(application)
                                        .withDevice(device)))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(mvcResult -> {
                    Response r = Utils.deserializeFrom(mvcResult.getResponse().getContentAsString(), Response.class);
                    assertEquals(201, r.getCode().intValue());
                    assertEquals(errorMessageAdd, r.getDescription());
                });

        mockMvc.perform(post("/registry/remove")
                .content(
                        mapper.writeValueAsString(
                                new RegisterDeviceRequest()
                                        .withApplication(application)
                                        .withDevice(device)))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(mvcResult -> {
                    Response r = Utils.deserializeFrom(mvcResult.getResponse().getContentAsString(), Response.class);
                    assertEquals(201, r.getCode().intValue());
                    assertEquals(errorMessageRemove, r.getDescription());
                });

        mockMvc.perform(post("/registry/assign")
                .content(
                        mapper.writeValueAsString(
                                new RegisterUserRequest()
                                        .withApplication(application)
                                        .withDevice(device)
                                        .withUser(user)))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(mvcResult -> {
                    Response r = Utils.deserializeFrom(mvcResult.getResponse().getContentAsString(), Response.class);
                    assertEquals(201, r.getCode().intValue());
                    assertEquals(errorMessageAssign, r.getDescription());
                });
    }

    @Test
    public void success() throws Exception {
        final String successMessage = "OK";

        when(registry.add(any(Application.class), any(Device.class)))
                .thenReturn(new Reply(200, successMessage));
        when(registry.remove(any(Application.class), any(Device.class)))
                .thenReturn(new Reply(200, successMessage));
        when(registry.assign(any(User.class), any(Application.class), any(Device.class)))
                .thenReturn(new Reply(200, successMessage));

        mockMvc.perform(post("/registry/add")
                .content(
                        mapper.writeValueAsString(
                                new RegisterDeviceRequest()
                                        .withApplication(application)
                                        .withDevice(device)))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(mvcResult -> {
                    RegisterDeviceResponse r = Utils.deserializeFrom(
                            mvcResult
                                    .getResponse()
                                    .getContentAsString(),
                            RegisterDeviceResponse.class);
                    assertEquals(200, r.getCode().intValue());
                    assertEquals(successMessage, r.getDescription());
                });

        mockMvc.perform(post("/registry/remove")
                .content(
                        mapper.writeValueAsString(
                                new RegisterDeviceRequest()
                                        .withApplication(application)
                                        .withDevice(device)))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(mvcResult -> {
                    RegisterDeviceResponse r = Utils.deserializeFrom(
                            mvcResult
                                    .getResponse()
                                    .getContentAsString(),
                            RegisterDeviceResponse.class);
                    assertEquals(200, r.getCode().intValue());
                    assertEquals(successMessage, r.getDescription());
                });

        mockMvc.perform(post("/registry/assign")
                .content(
                        mapper.writeValueAsString(
                                new RegisterUserRequest()
                                        .withApplication(application)
                                        .withDevice(device)
                                        .withUser(user)))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(mvcResult -> {
                    RegisterUserResponse r = Utils.deserializeFrom(
                            mvcResult
                                    .getResponse()
                                    .getContentAsString(),
                            RegisterUserResponse.class);
                    assertEquals(200, r.getCode().intValue());
                    assertEquals(successMessage, r.getDescription());
                });
    }
}
